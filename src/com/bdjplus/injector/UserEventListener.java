package com.bdjplus.injector;

import com.sony.bdjstack.core.CoreXletContext;
import com.sony.bdjstack.system.BDJModule;
import org.dvb.event.EventManager;
import org.dvb.event.UserEvent;

public class UserEventListener implements org.dvb.event.UserEventListener {
    private int keyIndex = 0;
    private int[] keyPattern = new int[]{37, 37, 461, 39, 39};
    public void userEventReceived(UserEvent userEvent)
    {
        // 37 - left
        // 461 - square
        // 39 - right
        if(userEvent.getType() == 401)
        {
            if(userEvent.getCode() == keyPattern[keyIndex])
            {
                keyIndex++;
                if(keyIndex == keyPattern.length)
                {
                    BDJModule.log("Key pattern matched!");

                    EventManager.getInstance().removeUserEventListener(this);

                    BDJModule.getInstance().terminateTitle(BDJModule.getInstance().getCurrentTitle());
                    BDJModule.getInstance().terminateTitle(0);
                    InjectorXlet instance = new InjectorXlet();
                    instance.initXlet(new CoreXletContext(0, new String[0], 0, 0));
                    instance.startXlet();

                    keyIndex = 0;
                }
            }else{
                keyIndex = 0;
            }
        }else if(userEvent.getType() == 402){

        }
    }
}
