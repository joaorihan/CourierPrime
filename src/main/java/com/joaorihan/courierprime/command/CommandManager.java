package com.joaorihan.courierprime.command;

import com.joaorihan.courierprime.command.config.MainConfig;

public class CommandManager {


    public CommandManager() {
        registerCommands();
    }


    private void registerCommands(){
        new AdminCommand();
        new BlockCommand();
        new HelpCommand();
        new InspectCommand();
        new LetterCommand();
        new PostCommand();
        new ShredCommand();
        new UnreadCommand();

        if (MainConfig.isANONYMOUS_LETTERS())
            new AnonymousLetterCommand();

    }


}
