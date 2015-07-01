/*
 * Copyright 2014-2015 Yahoo! Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.yahoo.validatar;

import java.util.List;

import static org.mockito.Mockito.*;
import org.mockito.ArgumentCaptor;
import org.apache.log4j.Level;
import org.apache.log4j.Appender;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;

/**
 * Extend this class to capture logging output without printing to stdout
 * Call setupMockedAppender and teardownMockedAppender to start and stop
 * the capturing and use isStringInLog to check if desired output in log
 * was seen.
 */
public class LogCaptor {
    protected Appender mockedAppender;
    protected Level originalLevel;
    protected Appender originalAppender;

    private boolean contains(String source, String target, boolean isCaseSensitive) {
        String superString = source;
        String subString = target;
        if (!isCaseSensitive) {
            superString = superString.toLowerCase();
            subString = subString.toLowerCase();
        }
        return superString.contains(subString);
    }

    protected boolean isStringInLog(String message) {
        return isStringInLog(message, false);
    }

    protected boolean isStringInLog(String message, boolean isCaseSensitive) {
        ArgumentCaptor<LoggingEvent> arguments = ArgumentCaptor.forClass(LoggingEvent.class);
        verify(mockedAppender, atLeastOnce()).doAppend(arguments.capture());
        List<LoggingEvent> events = arguments.getAllValues();
        for (LoggingEvent event : events) {
            String log = event.getRenderedMessage();
            if (contains(log, message, isCaseSensitive)) {
                return true;
            }
        }
        return false;
    }

    protected boolean noStringInLog() {
        ArgumentCaptor<LoggingEvent> arguments = ArgumentCaptor.forClass(LoggingEvent.class);
        try {
            verify(mockedAppender, never()).doAppend(arguments.capture());
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    protected void setupMockedAppender() {
        setupMockedAppender(Level.ERROR);
    }

    protected void setupMockedAppender(Level level) {
        originalLevel = Logger.getRootLogger().getLevel();
        originalAppender = Logger.getRootLogger().getAppender("stdout");
        Logger.getRootLogger().removeAllAppenders();
        mockedAppender = mock(Appender.class);
        Logger.getRootLogger().setLevel(level);
        Logger.getRootLogger().addAppender(mockedAppender);
    }

    protected void teardownMockedAppender() {
        Logger.getRootLogger().removeAllAppenders();
        Logger.getRootLogger().setLevel(originalLevel);
        Logger.getRootLogger().addAppender(originalAppender);
    }
}
