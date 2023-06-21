package com.zenyte.network.login.codec;

import com.zenyte.game.HardwareInfo;
import com.zenyte.game.util.Utils;
import com.zenyte.game.world.entity.player.Device;
import com.zenyte.network.ClientResponse;
import com.zenyte.network.NetworkConstants;
import com.zenyte.network.io.ByteBufUtil;
import com.zenyte.network.io.security.ISAACCipher;
import com.zenyte.network.io.security.ISAACCipherPair;
import com.zenyte.network.login.packet.LoginPacketIn;
import com.zenyte.network.login.packet.inc.LoginType;
import com.zenyte.utils.Ordinal;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * @author Tommeh | 27 jul. 2018 | 19:21:02
 * @see <a href="https://www.rune-server.ee/members/tommeh/">Rune-Server
 * profile</a>}
 */
public final class LoginDecoder extends ByteToMessageDecoder {

    @Ordinal
    public enum AuthType {
        TRUSTED_COMPUTER, TRUSTED_AUTHENTICATION, NORMAL, UNTRUSTED_AUTHENTICATION;
        private static final AuthType[] values = values();

        private AuthType() {
        }
    }

    @Override
    protected void decode(final ChannelHandlerContext ctx, final ByteBuf in, final List<Object> out) throws Exception {
        if (in.readableBytes() < 3) {
            return;
        }
        in.markReaderIndex();
        final com.zenyte.network.login.packet.inc.LoginType type = LoginType.get(in.readUnsignedByte());
        final int size = in.readUnsignedShort();
        if (in.readableBytes() < size) {
            in.resetReaderIndex();
            return;
        }
        final int version = in.readInt();
        final int subVersion = in.readInt();
        //in.readUnsignedByte(); Commenting out; We didn't use this before however we use this same exact index in buffer to determine mac address length.
        final short macLength = in.readUnsignedByte();
        final com.zenyte.game.world.entity.player.Device device = macLength == 2 ? Device.MOBILE : Device.DESKTOP;
        StringBuilder macBuilder = new StringBuilder();
        if (device.equals(Device.DESKTOP)) {
            final byte[] mac = new byte[macLength];
            for (int i = 0; i < macLength; i++) {
                mac[i] = (byte) in.readUnsignedByte();
            }
            for (int i = 0; i < mac.length; i++) {
                macBuilder.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
            }
        }
        final io.netty.buffer.ByteBuf rsaBuf = ByteBufUtil.encipherRSA(in, NetworkConstants.RSA_EXPONENT, NetworkConstants.RSA_MODULUS);
        if (rsaBuf == null) {
            ctx.writeAndFlush(ClientResponse.MALFORMED_LOGIN_PACKET).addListener(ChannelFutureListener.CLOSE);
            return;
        }
        final short rsaKey = rsaBuf.readUnsignedByte();
        final int[] xteaKeys = new int[4];
        final int[] serverKeys = new int[4];
        for (int i = 0; i < 4; i++) {
            xteaKeys[i] = rsaBuf.readInt();
            serverKeys[i] = xteaKeys[i] + 50;
        }
        final long serverSeed = rsaBuf.readLong();
        AuthType authType = null;
        int authenticatorCode = -1;
        int pcIdentifier = -1;
        final int[] previousXteaKeys = new int[4];
        String password = "";
        if (type.equals(LoginType.RECONNECT_LOGIN_CONNECTION)) {
            for (int i = 0; i < 4; i++) {
                previousXteaKeys[i] = rsaBuf.readInt();
            }
        } else {
            authType = AuthType.values[rsaBuf.readUnsignedByte()];
            if (authType == AuthType.NORMAL) {
                rsaBuf.skipBytes(4);
            } else if (authType == AuthType.UNTRUSTED_AUTHENTICATION || authType == AuthType.TRUSTED_AUTHENTICATION) {
                authenticatorCode = rsaBuf.readUnsignedMedium();
                rsaBuf.skipBytes(1);
            } else if (authType == AuthType.TRUSTED_COMPUTER) {
                pcIdentifier = rsaBuf.readInt();
            }
            rsaBuf.skipBytes(1);
            password = ByteBufUtil.readString(rsaBuf);
        }
        final io.netty.buffer.ByteBuf xteaBuf = ByteBufUtil.decipherXTEA(in, xteaKeys);
        final java.lang.String username = ByteBufUtil.readString(xteaBuf);
        final short clientProperties = xteaBuf.readUnsignedByte();
        final boolean lowMemory = (clientProperties & 1) == 1;
        final int mode = clientProperties >> 1;
        final int width = xteaBuf.readUnsignedShort();
        final int height = xteaBuf.readUnsignedShort();
        xteaBuf.skipBytes(24); // cacheUID
        final java.lang.String sessionToken = ByteBufUtil.readString(xteaBuf);
        final int affiliateId = xteaBuf.readInt();
        final com.zenyte.game.HardwareInfo hardwareInfo = new HardwareInfo(xteaBuf);
        final boolean supportsJs = xteaBuf.readUnsignedByte() == 1;
        if (device.equals(Device.MOBILE)) {
            xteaBuf.readUnsignedByte();
        }
        xteaBuf.readInt();
        final int[] crc = new int[Math.min(255, xteaBuf.readableBytes() / 4)];
        for (int i = 0; i < crc.length; i++) {
            crc[i] = xteaBuf.readInt();
        }
        final com.zenyte.network.io.security.ISAACCipherPair isaacPair = new ISAACCipherPair(new ISAACCipher(serverKeys), new ISAACCipher(xteaKeys));
        out.add(new LoginPacketIn(type, version, subVersion, Utils.formatUsername(username), password, mode, crc, sessionToken, authenticatorCode, pcIdentifier, authType, hardwareInfo, isaacPair, rsaKey, xteaKeys, previousXteaKeys, macBuilder.toString(), device));
    }

    @Override
    public boolean isSingleDecode() {
        return true;
    }
}
