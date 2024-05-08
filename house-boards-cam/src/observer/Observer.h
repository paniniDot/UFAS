#ifndef __OBSERVER__
#define __OBSERVER__

#include "Event.h"

template <typename T>

class Observer
{
public:
    virtual void update(Event<T> *e) = 0;
};

#endif