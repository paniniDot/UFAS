#ifndef __EVENT__
#define __EVENT__

#include "EventSourceType.h"
#include "Arduino.h"
template <typename T>

class Event
{

private:
    EventSourceType srcType;
    T *eventArgs;

public:
    Event(EventSourceType srctype, T *eventArgs)
    {
        this->srcType = srctype;
        this->eventArgs = eventArgs;
    }

    EventSourceType getSrcType()
    {
        return srcType;
    }

    T *getEventArgs()
    {
        return eventArgs;
    }

    ~Event()
    {
        delete this->eventArgs;
    }
};

#endif