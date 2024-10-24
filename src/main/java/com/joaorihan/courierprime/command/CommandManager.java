package com.joaorihan.courierprime.command;

import com.joaorihan.courierprime.config.MainConfig;

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

        if (MainConfig.isAnonymousLetters())
            new AnonymousLetterCommand();

    }


}
