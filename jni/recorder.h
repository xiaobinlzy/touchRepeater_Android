/*
 * recorder.h
 *
 *  Created on: 2014年6月13日
 *      Author: DLL
 */

#include <fcntl.h>
#include <stdio.h>
#include <poll.h>
#include <linux/input.h>
#include <string.h>
#include <unistd.h>

#include "jni_log.h"
#include "events.h"

#ifndef RECORDER_H_
#define RECORDER_H_

struct recorder
{
	int is_recording;
	int out_fd;
	struct pollfd *in_fds;
};

void destroy(struct recorder* recorder);

struct recorder* init(const char* filePath)
{
	char buffer[256];
	int fd, i;
	struct recorder *recorder = malloc(sizeof(struct recorder));
	memset(recorder, 0, sizeof(recorder));
	struct pollfd *in_fds = malloc(NUM_DEVICES * sizeof(struct pollfd));
	recorder->in_fds = in_fds;

	recorder->out_fd = open(filePath, O_WRONLY | O_CREAT | O_TRUNC);
	if (recorder->out_fd < 0)
	{
		LOGD("Couldn't open output file: %s", filePath);
		destroy(recorder);
		return 0;
	}
	for (i = 0; i < NUM_DEVICES; i++)
	{
		sprintf(buffer, "%s%s", event_prefix, event_devices[i]);
		recorder->in_fds[i].events = POLLIN;
		recorder->in_fds[i].fd = open(buffer, O_RDONLY | O_NDELAY);
		if (recorder->in_fds[i].fd < 0)
		{
			LOGD("Couldn't open input device %s", buffer);
			destroy(recorder);
			return 0;
		}
	}
	recorder->is_recording = 1;
	LOGD("recorder init ok");
	return recorder;
}

void stop(struct recorder * recorder)
{
	recorder->is_recording = 0;
}

int record(struct recorder * recorder)
{
	int i, num_read;
	struct input_event event;
	while (recorder->is_recording)
	{
		if (poll(recorder->in_fds, NUM_DEVICES, -1) < 0)
		{
			LOGD("poll error");
			return 1;
		}
		if (recorder->is_recording)
		{
			for (i = 0; i < NUM_DEVICES; i++)
			{
				if (recorder->in_fds[i].revents & POLLIN)
				{
					num_read = read(recorder->in_fds[i].fd, &event, sizeof(event));
					if (num_read != sizeof(event))
					{
						LOGD("read error");
						return 2;
					}
					if ((write(recorder->out_fd, &i, sizeof(i)) != sizeof(i)
							|| write(recorder->out_fd, &event, sizeof(event)) != sizeof(event)))
					{
						LOGD("Write error\n");
						return 3;
					}
					LOGD("event: %d %08x %08x %08x", i, event.type, event.code, event.value);
				}
			}
		}
		else
		{
			break;
		}
	}
	return 0;
}

void closeFiles(struct recorder* recorder)
{
	int result = 0, i;
	for (i = 0; i < NUM_DEVICES; i++)
	{
		if (recorder->in_fds[i].fd > 0)
		{
			result += close(recorder->in_fds[i].fd);
		}
	}
	if (recorder->out_fd > 0)
	{
		result += close(recorder->out_fd);
	}
	LOGD("close file: %d", result);
}

void destroy(struct recorder* recorder)
{
	closeFiles(recorder);
	free(recorder->in_fds);
	free(recorder);
}

#endif /* RECORDER_H_ */
