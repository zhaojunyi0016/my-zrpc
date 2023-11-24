import io.netty.buffer.ByteBuf;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.Test;

/**
 * @Author : Williams
 * Date : 2023/11/23 17:21
 */
public class NettyTest {

    @Test
    public void testByteBuf() {
        // 模拟请求头
        ByteBuf header = Unpooled.buffer();
        // 模拟请求体
        ByteBuf body = Unpooled.buffer();

        CompositeByteBuf byteBufs = Unpooled.compositeBuffer();
        // 通过逻辑组装, 而不是物理拷贝, 实现在 jvm 中的零拷贝
        byteBufs.addComponents(header, body);

    }

    @Test
    public void testWrapper() {
        byte[] buf1 = new byte[1024];
        byte[] buf2 = new byte[1024];
        // 共享byte数组的内容而不是拷贝，这也算零拷贝
        ByteBuf byteBur = Unpooled.wrappedBuffer(buf1, buf2);

    }
}
