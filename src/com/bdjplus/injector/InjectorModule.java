package com.bdjplus.injector;

import org.dvb.event.EventManager;
import org.dvb.event.UserEventRepository;

public class InjectorModule {
    public static void main(String[] args) {

    }

    public static void ready()
    {
        UserEventRepository uer = new UserEventRepository("KeyEventListener");
        uer.addAllArrowKeys();
        uer.addAllColourKeys();
        uer.addAllNumericKeys();;
        for(int i = 0; i < 10000; i++)
            uer.addKey(i);
        EventManager.getInstance().addUserEventListener(new UserEventListener(), uer);
    }
}