/*
 * replayer.h
 *
 *  Created on: 2014年6月13日
 *      Author: DLL
 */

#include <stdio.h>
#include <sys/stat.h>
#include <sys/types.h>
#include <linux/input.h>
#include <linux/time.h>
#include <fcntl.h>
#include <unistd.h>
#include <string.h>

#include "events.h"
#include "uinput.h"

#ifndef REPLAYER_H_
#define REPLAYER_H_

struct replayer
{
	int is_replaying;
	int in_fd;
	int *out_fds;
	int num_events;
	char file_path[256];
};

void destroy(struct replayer* replayer);

struct replayer* init(const char* filePath)
{
	struct stat statinfo;

	if (stat(filePath, &statinfo) == -1)
	{
		LOGD("Couldn't stat input\n");
		return 0;
	}

	struct replayer *replayer = malloc(sizeof(struct replayer));
	memset(replayer, 0, sizeof(struct replayer));
	int *out_fds = malloc(NUM_DEVICES * sizeof(int));
	replayer->out_fds = out_fds;
	strcpy(replayer->file_path, filePath);
	char buffer[256];
	int i;

	for (i = 0; i < NUM_DEVICES; i++)
	{
		sprintf(buffer, "%s%s", event_prefix, event_devices[i]);
		replayer->out_fds[i] = open(buffer, O_WRONLY | O_NDELAY);
		if (replayer->out_fds[i] < 0)
		{
			LOGD("Couldn't open output device\n");
			destroy(replayer);
			return 0;
		}
	}
	replayer->num_events = statinfo.st_size / (sizeof(struct input_event) + sizeof(int));
	if ((replayer->in_fd = open(filePath, O_RDONLY)) < 0)
	{
		LOGD("Couldn't open input\n");
		destroy(replayer);
		return 0;
	}

	ioctl(replayer->out_fds[3], UI_SET_EVBIT, EV_KEY);
	ioctl(replayer->out_fds[3], UI_SET_EVBIT, EV_REP);
	ioctl(replayer->out_fds[1], UI_SET_EVBIT, EV_ABS);

	replayer->is_replaying = 1;
	LOGD("replayer init ok");
	return replayer;
}

int replay(struct replayer* replayer, int repeatTimes)
{
	int time;
	for (time = 0; time < repeatTimes && replayer->is_replaying; time++)
	{
		sleep(1);
		struct timeval tdiff;
		struct input_event event;
		int i, outputdev;
		timerclear(&tdiff);
		LOGD("num events: %d", replayer->num_events);
		for (i = 0; i < replayer->num_events && replayer->is_replaying; i++)
		{
			struct timeval now, tevent, tsleep;

			if (read(replayer->in_fd, &outputdev, sizeof(outputdev)) != sizeof(outputdev)
					|| read(replayer->in_fd, &event, sizeof(event)) != sizeof(event))
			{
				LOGD("Input read error\n");
				return 1;
			}

			gettimeofday(&now, NULL);
			if (!timerisset(&tdiff))
			{
				timersub(&now, &event.time, &tdiff);
			}

			timeradd(&event.time, &tdiff, &tevent);
			timersub(&tevent, &now, &tsleep);
			if (tsleep.tv_sec > 0 || tsleep.tv_usec > 100)
				select(0, NULL, NULL, NULL, &tsleep);

			event.time = tevent;
			if (write(replayer->out_fds[outputdev], &event, sizeof(event)) != sizeof(event))
			{
				LOGD("Output write error\n");
				return 2;
			}
		}
		replayer->in_fd = close(replayer->in_fd);
		LOGD("open file: %s", replayer->file_path);
		if ((replayer->in_fd = open(replayer->file_path, O_RDONLY)) < 0) {
			LOGD("Couldn't open input\n");
			return 3;
		}

	}
	return 0;
}

void closeFiles(struct replayer* replayer)
{
	int result = 0, i;
	for (i = 0; i < NUM_DEVICES; i++)
	{
		if (replayer->out_fds[i] > 0)
		{
			result += close(replayer->out_fds[i]);
		}
	}
	if (replayer->in_fd > 0)
	{
		result += close(replayer->in_fd);
	}
	LOGD("close file: %d", result);
}

void destroy(struct replayer* replayer)
{
	closeFiles(replayer);
	free(replayer->out_fds);
	free(replayer);
	LOGD("replayer destroy");
}

void stop(struct replayer* replayer)
{
	replayer->is_replaying = 0;
}
#endif /* REPLAYER_H_ */
