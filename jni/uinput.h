/* Android munges Linux headers to avoid copyright issues, but doesn't munge linux/uinput.h,
 * so constants reproduced here.
*/

#ifndef __UINPUT__
#define __UINPUT__


#define UI_SET_EVBIT   0x40045564
#define UI_SET_KEYBIT  0x40045565
#define UI_SET_RELBIT  0x40045566
#define UI_SET_ABSBIT  0x40045567

#define UINPUT_MAX_NAME_SIZE	80

struct uinput_id {
	uint16_t bustype;
	uint16_t vendor;
	uint16_t product;
	uint16_t version;
};

struct uinput_dev {
	char name[UINPUT_MAX_NAME_SIZE];
	struct uinput_id id;
	int ff_effects_max;
	int absmax[ABS_MAX + 1];
	int absmin[ABS_MAX + 1];
	int absfuzz[ABS_MAX + 1];
	int absflat[ABS_MAX + 1];
};

struct uinput_event {
	struct timeval time;
	uint16_t type;
	uint16_t code;
	int32_t value;
};


#endif	/* __UINPUT__ */
