package i.notepad.util;

import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;

/**
 * 文件工具
 *
 * @author 511721050589
 */

public class FileUtils {

    private static final String TAG = "FileUtils";

    private FileUtils() {
    }

    /**
     * 写文本文件
     *
     * @param path 路径
     * @param text 要写入的文本
     */
    public static void writeTextFile(String path, String text) {
        try (FileOutputStream fos = new FileOutputStream(new File(path));
             FileChannel channel = fos.getChannel()) {
            ByteBuffer src = Charset.defaultCharset().encode(text);
            while (src.hasRemaining()) channel.write(src);
        } catch (IOException e) {
            Log.e(TAG, "writeTextFile: ", e);
        }
    }
}
