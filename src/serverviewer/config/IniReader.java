/*
 * Copyright (c) 2009 Christian Ratzenhofer <christian_ratzenhofer@yahoo.de>
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 * */
package serverviewer.config;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class to read a .ini file to the memory
 */
public class IniReader {

    private ArrayList<HashMap<String, String>> sections = new ArrayList<HashMap<String, String>>();

    public IniReader(String path) throws FileNotFoundException, IOException {
        BufferedReader fileReader = new BufferedReader(new FileReader(path));
        Pattern iniLine = Pattern.compile("^([^=]+)=([^=]+)$");
        Pattern iniSection = Pattern.compile("^\\s*\\[([^\\[\\]]+)\\]\\s*$");
        String line;
        String section = null;
        Matcher matcher;
        HashMap<String, String> lines = new HashMap<String, String>();
        while ((line = fileReader.readLine()) != null) {
            if (!line.trim().startsWith("//")) {
                matcher = iniSection.matcher(line);
                if (matcher.matches()) {
                    if (lines.size() != 0) {
                        lines.put("name", section);
                        sections.add(lines);
                        lines = new HashMap<String, String>();
                    }
                    section = matcher.group(1);
                } else {
                    matcher = iniLine.matcher(line);
                    if (matcher.matches()) {
                        if (matcher.group(1).trim().equals("adress")) {
                            lines.put("address", matcher.group(2).trim());
                        } else {
                            lines.put(matcher.group(1).trim(), matcher.group(2).trim());
                        }
                    }
                }
            }
        }
        if (section != null) {
            if (!lines.containsKey("name")) {
                lines.put("name", section);
            }
            if (!sections.contains(lines)) {
                sections.add(lines);
            }
        }
    }

    /**
     * Returns the whole ini file.
     */
    public ArrayList<HashMap<String, String>> getIniContent() {
        return this.sections;
    }
}
