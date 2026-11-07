package io.nekohasekai.sagernet.fmt.musess;

import androidx.annotation.NonNull;

import com.esotericsoftware.kryo.io.ByteBufferInput;
import com.esotericsoftware.kryo.io.ByteBufferOutput;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import io.nekohasekai.sagernet.fmt.AbstractBean;
import io.nekohasekai.sagernet.fmt.KryoConverters;

/**
 * Musess (Wraith) protocol bean.
 *
 * Field names and defaults mirror the sing-box core option struct
 * MusessOutboundOptions (impl/sing-box/option/musess.go):
 * - warmPool is tri-state: null means "unset", and the core defaults an
 *   unset warm pool to size 2; explicit 0 disables the pool. It is stored
 *   as -1 in the Kryo stream when null.
 * - early_request is intentionally NOT a bean field: the core defaults it
 *   to on (unset and true both enable it), so the app simply never sends it.
 * - deshell / mux / muxAdaptiveBypass / fec default off in the core.
 */
public class MusessBean extends AbstractBean {

    public static final Creator<MusessBean> CREATOR = new CREATOR<MusessBean>() {
        @NonNull
        @Override
        public MusessBean newInstance() {
            return new MusessBean();
        }

        @Override
        public MusessBean[] newArray(int size) {
            return new MusessBean[size];
        }
    };

    // Borrowed destination site, presented as the TLS SNI (server_name).
    public String serverName;
    public String uuid;
    public String secret;
    // Server X25519 public key, base64-encoded (public_key).
    public String publicKey;

    public Boolean mux;
    // Destinations that skip the mux session (mux_bypass).
    public List<String> muxBypass;
    public Boolean muxAdaptiveBypass;
    // Tri-state warm pool size: null = core default (2), 0 = off.
    public Integer warmPool;
    public Boolean deshell;
    // "off" / "auto" / "on".
    public String fec;

    @Override
    public void initializeDefaultValues() {
        super.initializeDefaultValues();
        if (serverName == null) serverName = "";
        if (uuid == null) uuid = "";
        if (secret == null) secret = "";
        if (publicKey == null) publicKey = "";
        if (mux == null) mux = false;
        if (muxBypass == null) muxBypass = new ArrayList<>();
        if (muxAdaptiveBypass == null) muxAdaptiveBypass = false;
        // warmPool stays null: the core defaults an unset pool to size 2.
        if (deshell == null) deshell = false;
        if (fec == null) fec = "off";
    }

    @Override
    public void serialize(ByteBufferOutput output) {
        output.writeInt(0);
        super.serialize(output);
        output.writeString(serverName);
        output.writeString(uuid);
        output.writeString(secret);
        output.writeString(publicKey);
        output.writeBoolean(mux);
        output.writeInt(muxBypass.size());
        for (String host : muxBypass) {
            output.writeString(host);
        }
        output.writeBoolean(muxAdaptiveBypass);
        output.writeInt(warmPool == null ? -1 : warmPool);
        output.writeBoolean(deshell);
        output.writeString(fec);
    }

    @Override
    public void deserialize(ByteBufferInput input) {
        int version = input.readInt();
        super.deserialize(input);
        serverName = input.readString();
        uuid = input.readString();
        secret = input.readString();
        publicKey = input.readString();
        mux = input.readBoolean();
        int bypassSize = input.readInt();
        muxBypass = new ArrayList<>();
        for (int i = 0; i < bypassSize; i++) {
            muxBypass.add(input.readString());
        }
        muxAdaptiveBypass = input.readBoolean();
        int warmPoolValue = input.readInt();
        warmPool = warmPoolValue < 0 ? null : warmPoolValue;
        deshell = input.readBoolean();
        fec = input.readString();
    }

    @NotNull
    @Override
    public MusessBean clone() {
        return KryoConverters.deserialize(new MusessBean(), KryoConverters.serialize(this));
    }
}
