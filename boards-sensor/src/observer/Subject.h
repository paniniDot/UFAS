#ifndef __SUBJECT__
#define __SUBJECT__

#include "Observer.h"

template <typename T>

#define MAX_OBSERVERS 10

class Subject
{
private:
    Observer<T> *observers[MAX_OBSERVERS];
    int nObservers = 0;

public:
    virtual void attach(Observer<T> *o)
    {
        if (this->nObservers < MAX_OBSERVERS - 1)
        {
            this->observers[this->nObservers] = o;
            this->nObservers++;
        }
    }
    virtual void detach(Observer<T> *o)
    {
        for (int i = 0; i < this->nObservers; i++)
        {
            if (this->observers[i] == o)
            {
                this->observers[i] = this->observers[this->nObservers - 1];
                this->nObservers--;
                break;
            }
        }
    }
    virtual void notify() = 0;

    Observer<T> **getObservers()
    {
        return this->observers;
    }

    int getNObservers()
    {
        return this->nObservers;
    }
};

#endif