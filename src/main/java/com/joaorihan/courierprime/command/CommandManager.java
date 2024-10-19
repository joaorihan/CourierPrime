package com.joaorihan.courierprime.command;

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
    }


}
