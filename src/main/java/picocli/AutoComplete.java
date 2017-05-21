/*
   Copyright 2017 Remko Popma

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package picocli;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Generates a bash auto-complete script.
 */
public class AutoComplete {
    private final CommandLine commandLine;

    AutoComplete(Object annotatedObject) {
        this(new CommandLine(annotatedObject));
    }
    AutoComplete(CommandLine commandLine) {
        if (commandLine == null) { throw new NullPointerException("commandLine"); }
        this.commandLine = commandLine;
    }

    private static final String HEADER = "" +
            "#!bash\n" +
            "#\n" +
            "# %1s Bash Completion\n" +
            "# =======================\n" +
            "#\n" +
            "# Bash completion support for %1s,\n" +
            "# generated by [picocli](http://picocli.info/).\n" +
            "#\n" +
            "# Installation\n" +
            "# ------------\n" +
            "#\n" +
            "# 1. Place it in a `bash-completion.d` folder:\n" +
            "#\n" +
            "#   * /etc/bash-completion.d\n" +
            "#   * /usr/local/etc/bash-completion.d\n" +
            "#   * ~/bash-completion.d\n" +
            "#\n" +
            "# 2. Open new bash, and type `%1s [TAB][TAB]`\n" +
            "#\n" +
            "# Documentation\n" +
            "# -------------\n" +
            "# The script is called by bash whenever [TAB] or [TAB][TAB] is pressed after\n" +
            "# '%1s (..)'. By reading entered command line parameters, it determines possible\n" +
            "# bash completions and writes them to the COMPREPLY variable. Bash then\n" +
            "# completes the user input if only one entry is listed in the variable or\n" +
            "# shows the options if more than one is listed in COMPREPLY.\n" +
            "#\n" +
            "# The script first determines the current parameter ($cur), the previous\n" +
            "# parameter ($prev), the first word ($firstword) and the last word ($lastword).\n" +
            "# Using the $firstword variable (= the command) and a giant switch/case,\n" +
            "# completions are written to $complete_words and $complete_options.\n" +
            "#\n" +
            "# If the current user input ($cur) starts with '-', only $command_options are\n" +
            "# displayed/completed, otherwise only $command_words.\n" +
            "#\n" +
            "# References\n" +
            "# ----------\n" +
            "# [1] http://stackoverflow.com/a/12495480/1440785\n" +
            "# [2] http://tiswww.case.edu/php/chet/bash/FAQ\n" +
            "#\n" +
            "\n" +
            "shopt -s progcomp\n" +
            "_%1s() {\n";

    String bash() {
        final Map<String, Object> commands = commandLine.getCommands();
        Object annotated = commandLine.getAnnotatedObject();
        List<Field> requiredFields = new ArrayList<Field>();
        Map<String, Field> optionName2Field = new HashMap<String, Field>();
        Map<String, Field> singleCharOption2Field = new HashMap<String, Field>();
        List<Field> positionalParameterFields = new ArrayList<Field>();
        Class<?> cls = annotated.getClass();
        while (cls != null) {
            CommandLine.init(cls, requiredFields, optionName2Field, singleCharOption2Field, positionalParameterFields);
            cls = cls.getSuperclass();
        }
        String result = "";
        result += String.format(HEADER, scriptName);
        result += commandListDeclaration(commands);
        result += globalOptionDeclaration(optionName2Field);

        return result;
    }

    private String commandListDeclaration(final Map<String, Object> commands) {
        StringBuilder result = new StringBuilder("    GLOBAL_COMMANDS=\"\\\n");
        for (String key : commands.keySet()) {
            result.append("        ").append(key).append("\\\n");
        }
        result.setCharAt(result.length() - 2, '\"');
        return result.toString();
    }

    private String globalOptionDeclaration(final Map<String, ?> options) {
        StringBuilder result = new StringBuilder("    GLOBAL_OPTIONS=\"\\\n");
        for (String key : options.keySet()) {
            result.append("        ").append(key).append("\\\n");
        }
        result.setCharAt(result.length() - 2, '\"');
        return result.toString();
    }
}
