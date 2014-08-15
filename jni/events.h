/*
 * events.h
 *
 *  Created on: 2014年6月13日
 *      Author: DLL
 */

#ifndef EVENTS_H_
#define EVENTS_H_

const char *event_prefix = "/dev/input/";
const char *event_devices[] = { "event0", "event1", "event2", "event3", "event4", "event5", "event6", "event7" };
#define NUM_DEVICES (sizeof(event_devices) / sizeof(char *))
#endif /* EVENTS_H_ */
