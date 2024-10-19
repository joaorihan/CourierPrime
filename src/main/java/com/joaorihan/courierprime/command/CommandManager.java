package com.joaorihan.courierprime.command;

import com.joaorihan.courierprime.courier.CourierOptions;

public class CommandManager {


    public CommandManager() {
        registerCommands();
    }


    private void registerCommands(){
        new AdminCommand();
        new HelpCommand();
        new LetterCommand();
        new PostCommand();
        new ShredCommand();
        new UnreadCommand();

        if (CourierOptions.isANONYMOUS_LETTERS())
            new AnonymousLetterCommand();

    }


}
