package com.ts.music.utils;


import com.ts.music.constants.MusicConstants;
import com.ts.music.entity.AudioLrcBean;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Music Lrc parser.
 */

public class MusicLrcRowParser {

    /**
     * Parse file lrc.
     *
     * @return MusicLrcRow　list
     */
    public static List<AudioLrcBean> parseLrcFromFile(File file) {
        if (file.exists()) {
            return parseInputStream(file);
        } else {
            return null;
        }
    }

    private static List<AudioLrcBean> parseInputStream(File file) {
        List<AudioLrcBean> audioLrcBeans = new ArrayList<>();
        BufferedReader bufferedReader = null;
        FileInputStream inputStream;
        BufferedInputStream bis;
        try {
            inputStream = new FileInputStream(file);
            bis = new BufferedInputStream(inputStream);
            bis.mark(MusicConstants.READ_LIMIT);
            //Find the first three bytes of the document and determine
            // the document type automatically.
            byte[] charsetsIdentification = new byte[MusicConstants.FIRST_THREE_BYTES];
            int read = bis.read(charsetsIdentification);
            bis.reset();
            if (charsetsIdentification[0] == (byte) MusicConstants.FIRST_BYTES_EF
                    && charsetsIdentification[1]
                    == (byte) MusicConstants.FIRST_BYTES_BB && charsetsIdentification[2]
                    == (byte) MusicConstants.FIRST_BYTES_BF) {
                bufferedReader = new BufferedReader(new InputStreamReader(bis,
                        StandardCharsets.UTF_8));
            } else if (charsetsIdentification[0] == (byte) MusicConstants.FIRST_BYTES_5B
                    && charsetsIdentification[1]
                    == (byte) MusicConstants.FIRST_BYTES_30 && charsetsIdentification[2]
                    == (byte) MusicConstants.FIRST_BYTES_30) {
                bufferedReader = new BufferedReader(new InputStreamReader(bis,
                        StandardCharsets.UTF_8));
            } else if (charsetsIdentification[0] == (byte) MusicConstants.FIRST_BYTES_FF
                    && charsetsIdentification[1] == (byte) MusicConstants.FIRST_BYTES_FE) {
                bufferedReader = new BufferedReader(new InputStreamReader(bis,
                        MusicConstants.CHARSET_UNICODE));
            } else if (charsetsIdentification[0] == (byte) MusicConstants.FIRST_BYTES_FE
                    && charsetsIdentification[1] == (byte) MusicConstants.FIRST_BYTES_FF) {
                bufferedReader = new BufferedReader(new InputStreamReader(bis,
                        StandardCharsets.UTF_16BE));
            } else if (charsetsIdentification[0] == (byte) MusicConstants.FIRST_BYTES_FF
                    && charsetsIdentification[1] == (byte) MusicConstants.FIRST_BYTES_FF) {
                bufferedReader = new BufferedReader(new InputStreamReader(bis,
                        StandardCharsets.UTF_16BE));
            } else {
                bufferedReader = new BufferedReader(new InputStreamReader(bis,
                        MusicConstants.CHARSET_GBK));
            }
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                List<AudioLrcBean> lrcList = parseLrc(line);
                if (lrcList != null && lrcList.size() != 0) {
                    audioLrcBeans.addAll(lrcList);
                }
            }
            sortMusicLrcRow(audioLrcBeans);
            return audioLrcBeans;
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return audioLrcBeans;
    }

    private static void sortMusicLrcRow(List<AudioLrcBean> audioLrcBeans) {
        Collections.sort(audioLrcBeans, (o1, o2) -> (int) (o1.getTime() - o2.getTime()));
    }

    private static List<AudioLrcBean> parseLrc(String lrcLine) {
        if (lrcLine.trim().isEmpty()) {
            return null;
        }
        List<AudioLrcBean> audioLrcBeans = new ArrayList<>();
        Matcher matcher = Pattern.compile(MusicConstants.LINE_REGEX).matcher(lrcLine);
        if (!matcher.matches()) {
            return null;
        }

        String time = matcher.group(MusicConstants.MATCHER_GROUP_TIME);
        String content = matcher.group(MusicConstants.MATCHER_GROUP_CONTENT);
        Matcher timeMatcher = Pattern.compile(MusicConstants.TIME_REGEX).matcher(time);

        while (timeMatcher.find()) {
            String min = timeMatcher.group(MusicConstants.MATCHER_GROUP_TIME);
            String sec = timeMatcher.group(MusicConstants.MATCHER_GROUP_SEC);
            String mil = timeMatcher.group(MusicConstants.MATCHER_GROUP_CONTENT);
            AudioLrcBean lrc = new AudioLrcBean();
            if (content != null && content.length() != 0) {
                lrc.setTime(Long.parseLong(min) * MusicConstants.FORMATTER_MINUTE
                        * MusicConstants.FORMATTER_SECOND + Long.parseLong(sec)
                        * MusicConstants.FORMATTER_SECOND
                        + Long.parseLong(mil) * MusicConstants.MATCHER_GROUP_MIL);
                lrc.setContent(content);
                audioLrcBeans.add(lrc);
            }
        }
        return audioLrcBeans;
    }
}