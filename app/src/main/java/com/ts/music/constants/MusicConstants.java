package com.ts.music.constants;

/**
 * MusicConstants.
 */
public interface MusicConstants {

    // Distinguish　0:BtMusic, 1:OnlineMusic, 2:UsbMusic
    int BT_MUSIC = 0;
    int RADIO_MUSIC = 1;
    int USB_MUSIC = 2;

    int DEFAULT_PLAYER_INDEX = 0;

    /**
     * Play mode configuration.
     */
    // Single mode
    String SP_VALUE_MUSIC_MODEL_SINGLE = "sp_value_music_model_single";
    // List loop mode
    String SP_VALUE_MUSIC_MODEL_LOOP = "sp_value_music_model_loop";
    // Order play
    String SP_VALUE_MUSIC_MODEL_ORDER = "sp_value_music_model_order";
    // Random play
    String SP_VALUE_MUSIC_MODEL_RANDOM = "sp_value_music_model_random";

    // ParamsKey
    String KEY_PARAMS_MODEL = "key_params_model";

    /**
     * Various states inside the player.
     */
    // Ended or not started
    int MUSIC_PLAYER_STOP = 0;
    // In prepare
    int MUSIC_PLAYER_PREPARE = 1;
    // Buffer
    int MUSIC_PLAYER_BUFFER = 2;
    // In play
    int MUSIC_PLAYER_PLAYING = 3;
    // Pause
    int MUSIC_PLAYER_PAUSE = 4;
    // Error
    int MUSIC_PLAYER_ERROR = 5;

    // Player status, -1: Destroyed 1: Stop 2: Pause 3: Play 4: Prepare 5: Fail
    int PLAYER_STATUS_DEFAULT = -2;
    int PLAYER_STATUS_DESTROY = -1;
    int PLAYER_STATUS_STOP = 1;
    int PLAYER_STATUS_PAUSE = 2;
    int PLAYER_STATUS_START = 3;
    int PLAYER_STATUS_PREPARED = 4;
    int PLAYER_STATUS_ERROR = 5;

    /**
     * Identification of data source processed inside the player.
     */
    // Network
    int CHANNEL_NET = 0;
    // Local
    int CHANNEL_LOCATION = 1;
    int CHANNEL_LOCATION_LIST = 2;

    /**
     * Playback mode.
     */
    // List loop mode
    int MUSIC_MODEL_LOOP = 0;
    // Single mode
    int MUSIC_MODEL_SINGLE = 1;
    // Order play
    int MUSIC_MODEL_ORDER = 2;
    // Random play
    int MUSIC_MODEL_RANDOM = 3;

    /**
     * Time format.
     * Hour
     * Minute
     * Second
     * Millisecond
     */
    int FORMATTER_HOUR = 24;
    int FORMATTER_MINUTE = 60;
    int FORMATTER_SECOND = 1000;
    int FORMATTER_MILLISECOND = 3600;
    int CURRENT_DURATION = 500;
    int MAX_PROGRESS = 1000;
    int FORMAT_SECOND_BT_MUSIC = 500;

    // Input flags.
    int SOFT_INPUT_FLAGS = 0;
    //Parser lrc
    int MATCHER_GROUP_TIME = 1;
    int MATCHER_GROUP_SEC = 2;
    int MATCHER_GROUP_CONTENT = 3;
    int MATCHER_GROUP_MIL = 10;

    // Music Lrc view
    int LRC_TEXT_SIZE = 25;
    int ICON_LINE_GAP = 5;
    int INDICATOR_LINE_WIDTH = 1;
    int INDICATOR_TOUCH_DELAY = 2500;
    float OVER_SCROLLER = 0.1f;
    int PLAY_RECT_TOP = 2;
    float LRC_HEIGHT = 2f;
    int LINE_POSITION = 1;
    float TIME_WIDTH = 1.5f;
    float BASE_X = 1.1f;
    float STATIC_LAYOUT_SPACING_X = 1f;
    float STATIC_LAYOUT_SPACING_Y = 0f;
    int LRC_COUNT_INDEX = 1;
    int LRC_ANIMATOR_DURATION = 300;
    float LRC_MOVE_Y = 3.5f;
    int SCROLL_TO_POSITION = 0;
    String MUSIC_LRC_FILE_NAME = ".lrc";
    String CHARSET_GBK = "GBK";
    String CHARSET_UNICODE = "unicode";
    String LINE_REGEX = "((\\[\\d{2}:\\d{2}\\.\\d{2}])+)(.*)";
    String TIME_REGEX = "\\[(\\d{2}):(\\d{2})\\.(\\d{2})]";
    //Max number of usb
    int USB_LABEL_MAX_NUMBER = 2;
    String SETTING_PACKAGE_NAME = "com.ts.hmi.systemsettings";
    String SETTING_MAIN_ACTIVITY = "com.ts.hmi.systemsettings.SettingsActivity";
    String SETTING_BT_KEY = "module";
    String SETTING_BT_VALUE = "bluetooth";

    //Multiple hits in a second
    int CLICK_COUNTS = 1;
    int RECYCLE_VIEW_OFFSET = 2;
    int USB_1_INDEX = 0;
    int USB_2_INDEX = 1;
    String USB_1_PORT = "usb3";
    String USB_2_PORT = "usb1";

    int USB_1_PORT_ID = 1;
    int USB_2_PORT_ID = 3;

    String ONLINE_MUSIC_PACKAGE_NAME = "com.ts.onlinemedia";
    String ONLINE_MUSIC_MAIN_ACTIVITY = "com.ts.onlinemedia.MediaAppActivity";

    //Lyrics analysis related
    int READ_LIMIT = 4;
    int FIRST_THREE_BYTES = 3;
    int SUB_STRING_INDEX = 0;
    int FIRST_BYTES_EF = 0xEF;
    int FIRST_BYTES_BB = 0xBB;
    int FIRST_BYTES_BF = 0xBF;
    int FIRST_BYTES_FF = 0xFF;
    int FIRST_BYTES_FE = 0xFE;
    int FIRST_BYTES_5B = 0x5B;
    int FIRST_BYTES_30 = 0x30;
    // Folder ID
    int AUDIO_FOLDER_ID = -1;
    int FILE_DIRECTORY = 1;
    int MEDIA_SCANNER_STARTED = 1;
    int MEDIA_SCANNER_FINISHED = 2;
    int MEDIA_SCANNER_DATA = 3;
    String STR_SPLICER = " - ";
    String FILE_SIGN = "/";
    int UUID_INDEX = 2;
    int DELAY_HIDDEN_TIME = 2000;

    int ERROR_CODE = -1;

    class ErrorCode {
        public static final String UNKNOWN_ERROR = "1";
        // Received error (s) app must re instantiate new mediaplayer
        public static final String PLAYER_INTERNAL_ERROR = "2";
        // Stream start position error
        public static final String MEDIA_STREAMING_ERROR = "3";
        // IO,timeout error
        public static final String NETWORK_CONNECTION_TIMEOUT = "4";
        // 403
        public static final String PLAY_REQUEST_FAILED = "5";
    }

    String BT_MUSIC_EMPTY_NICKNAME = "";
    int DEFAULT_PROGRESS = 0;
    int FLAGS = 0;
    int VALID_SEEK_WIDTH = 510;
    int SEEK_TIP_OFFSET = 20;
    int PLAY_ALL = 1;
    int PLAY_CURRENT_FOLDER = 2;
    int BACK_USB_FOLDER = 3;
    int MIN_DEPTH = 3;
    int MAX_DEPTH = 9;
    String USB_FIRST_UUID = "USB_FIRST_UUID";
    String USB_SECOND_UUID = "USB_SECOND_UUID";
    int UPDATE_ADAPTER_FIRST = 1;
    int UPDATE_ADAPTER_SECOND = 2;
    int UPDATE_ADAPTER_ALL = 3;
}
