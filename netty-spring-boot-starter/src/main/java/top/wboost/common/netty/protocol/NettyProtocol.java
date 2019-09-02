package top.wboost.common.netty.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import top.wboost.common.base.enums.CharsetEnum;

import java.util.Arrays;
 
/**
 * <pre>
 *  数据包格式
 * +——----——+——-----——+——----——+
 * |协议开始标志|  长度             |   数据       |
 * +——----——+——-----——+——----——+
 * 1.协议开始标志head_data，为int类型的数据，16进制表示为0X76
 * 2.传输数据的长度contentLength，int类型
 * 3.要传输的数据
 * </pre>
 */
public class NettyProtocol {

    public static void addHandler(SocketChannel socketChannel) {
        //LengthFieldBasedFrameDecoder lengthFieldBasedFrameDecoder = new LengthFieldBasedFrameDecoder(1024, 4, 4);
        //socketChannel.pipeline().addLast(lengthFieldBasedFrameDecoder);
        socketChannel.pipeline().addLast(new DelimiterBasedFrameDecoder(NettyConstant.MAX_BYTES, Unpooled.copiedBuffer(NettyConstant.END_DATA)));
        socketChannel.pipeline().addLast(new NettyEncoder());
        socketChannel.pipeline().addLast(new NettyDecoder());
    }
	/**
	 * 消息的开头的信息标志
	 */
	private int head_data = NettyConstant.HEAD_DATA;
	/**
	 * 消息的长度
	 */
	private int contentLength;
	/**
     * 消息的结尾信息标志
     */
    private ByteBuf end_data = Unpooled.copiedBuffer(NettyConstant.END_DATA);
    /**
     * 消息的内容
     */
    private byte[] content;
 
	/**
	 * 用于初始化，NettyProtocol
	 * 
	 * @param contentLength
	 *            协议里面，消息数据的长度
	 * @param content
	 *            协议里面，消息的数据
	 */
	public NettyProtocol(int contentLength, byte[] content) {
		this.contentLength = contentLength;
		this.content = content;
	}

	public NettyProtocol(byte[] content) {
        this.contentLength = content.length;
		this.content = content;
	}
 
	public int getHead_data() {
		return head_data;
	}

	public String stringVale() {
	    return new String(getContent(), CharsetEnum.UTF_8.getCharset());
    }
 
	public int getContentLength() {
		return contentLength;
	}
 
	public void setContentLength(int contentLength) {
		this.contentLength = contentLength;
	}
 
	public byte[] getContent() {
		return content;
	}
 
	public void setContent(byte[] content) {
		this.content = content;
	}

    public ByteBuf getEnd_data() {
        return end_data;
    }

	@Override
	public String toString() {
		return "SmartCarProtocol [head_data=" + head_data + ", contentLength="
				+ contentLength + ", content=" + Arrays.toString(content) + "]";
	}
 
}