package ua.com.expert.validator.utils;

import android.content.Context;
import android.nfc.NfcAdapter;
import android.nfc.NfcManager;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.nfc.tech.NfcA;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcel;

import java.util.Locale;

public class NFCUtils {

    public static NfcAdapter getNFCAdapter(Context mContext){
        NfcManager manager = (NfcManager) mContext.getSystemService(Context.NFC_SERVICE);
        NfcAdapter adapter = manager.getDefaultAdapter();
        return adapter;
    }

    public static Boolean deviceHasNFC(Context mContext){
        if (getNFCAdapter(mContext) != null) {
            return true;
        }else{
            return false;
        }
    }

    public static Boolean enabledNFC(Context mContext){
        NfcAdapter adapter = getNFCAdapter(mContext);
        if (adapter != null && adapter.isEnabled()) {
            return true;
        }else{
            return false;
        }
    }

    public static String dumpTagData(Tag tag) {
        String[] techList;
        MifareClassic mifareTag;
        StringBuilder sb = new StringBuilder();
        byte[] id = tag.getId();
        sb.append("ID (hex): ").append(toHex(id)).append("\n");
        sb.append("ID (reversed hex): ").append(toReversedHex(id)).append("\n");
        sb.append("ID (dec): ").append(toDec(id)).append("\n");
        sb.append("ID (reversed dec): ").append(toReversedDec(id)).append("\n");
        sb.append("ID (Servio): ").append(getServioTagCode(id)).append("\n");
        String prefix = "android.nfc.tech.";
        sb.append("Technologies: ");
        for (String tech : tag.getTechList()) {
            sb.append(tech.substring(prefix.length()));
            sb.append(", ");
        }
        sb.delete(sb.length() - 2, sb.length());
        for (String tech2 : tag.getTechList()) {
            if (tech2.equals(MifareClassic.class.getName())) {
                sb.append(10);
                String type = "Unknown";
                try {
                    mifareTag = MifareClassic.get(tag);
                } catch (Exception e) {
                    tag = cleanupTag(tag);
                    mifareTag = MifareClassic.get(tag);
                }
                try {
                    switch (mifareTag.getType()) {
                        case 0:
                            type = "Classic";
                            break;
                        case 1:
                            type = "Plus";
                            break;
                        case 2:
                            type = "Pro";
                            break;
                    }
                    sb.append("Mifare Classic type: ");
                    sb.append(type);
                    sb.append(10);
                    sb.append("Mifare size: ");
                    sb.append(mifareTag.getSize() + " bytes");
                    sb.append(10);
                    sb.append("Mifare sectors: ");
                    sb.append(mifareTag.getSectorCount());
                    sb.append(10);
                    sb.append("Mifare blocks: ");
                    sb.append(mifareTag.getBlockCount());
                } catch (Exception e2) {
                    sb.append("Mifare classic error: " + e2.getMessage());
                }
            }
            if (tech2.equals(MifareUltralight.class.getName())) {
                sb.append(10);
                String type2 = "Unknown";
                switch (MifareUltralight.get(tag).getType()) {
                    case 1:
                        type2 = "Ultralight";
                        break;
                    case 2:
                        type2 = "Ultralight C";
                        break;
                }
                sb.append("Mifare Ultralight type: ");
                sb.append(type2);
            }
        }
        return sb.toString();
    }

    public static Tag cleanupTag(Tag oTag) {
        IBinder tagService;
        if (oTag == null) {
            return null;
        }
        String[] sTechList = oTag.getTechList();
        Parcel oParcel = Parcel.obtain();
        oTag.writeToParcel(oParcel, 0);
        oParcel.setDataPosition(0);
        int len = oParcel.readInt();
        byte[] id = null;
        if (len >= 0) {
            id = new byte[len];
            oParcel.readByteArray(id);
        }
        int[] oTechList = new int[oParcel.readInt()];
        oParcel.readIntArray(oTechList);
        Bundle[] oTechExtras = (Bundle[]) oParcel.createTypedArray(Bundle.CREATOR);
        int serviceHandle = oParcel.readInt();
        int isMock = oParcel.readInt();
        if (isMock == 0) {
            tagService = oParcel.readStrongBinder();
        } else {
            tagService = null;
        }
        oParcel.recycle();
        int nfca_idx = -1;
        int mc_idx = -1;
        short oSak = 0;
        short nSak = 0;
        for (int idx = 0; idx < sTechList.length; idx++) {
            if (sTechList[idx].equals(NfcA.class.getName())) {
                if (nfca_idx == -1) {
                    nfca_idx = idx;
                    if (oTechExtras[idx] != null && oTechExtras[idx].containsKey("sak")) {
                        oSak = oTechExtras[idx].getShort("sak");
                        nSak = oSak;
                    }
                } else if (oTechExtras[idx] != null && oTechExtras[idx].containsKey("sak")) {
                    nSak = (short) (oTechExtras[idx].getShort("sak") | nSak);
                }
            } else if (sTechList[idx].equals(MifareClassic.class.getName())) {
                mc_idx = idx;
            }
        }
        boolean modified = false;
        if (oSak != nSak) {
            oTechExtras[nfca_idx].putShort("sak", nSak);
            modified = true;
        }
        if (!(nfca_idx == -1 || mc_idx == -1 || oTechExtras[mc_idx] != null)) {
            oTechExtras[mc_idx] = oTechExtras[nfca_idx];
            modified = true;
        }
        if (!modified) {
            return oTag;
        }
        Parcel nParcel = Parcel.obtain();
        nParcel.writeInt(id.length);
        nParcel.writeByteArray(id);
        nParcel.writeInt(oTechList.length);
        nParcel.writeIntArray(oTechList);
        nParcel.writeTypedArray(oTechExtras, 0);
        nParcel.writeInt(serviceHandle);
        nParcel.writeInt(isMock);
        if (isMock == 0) {
            nParcel.writeStrongBinder(tagService);
        }
        nParcel.setDataPosition(0);
        Tag nTag = (Tag) Tag.CREATOR.createFromParcel(nParcel);
        nParcel.recycle();
        return nTag;
    }

    public static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = bytes.length - 1; i >= 0; i--) {
            int b = bytes[i] & 255;
            if (b < 16) {
                sb.append('0');
            }
            sb.append(Integer.toHexString(b));
            if (i > 0) {
                sb.append(" ");
            }
        }
        return sb.toString();
    }

    public static String toReversedHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            if (i > 0) {
                sb.append(" ");
            }
            int b = bytes[i] & 255;
            if (b < 16) {
                sb.append('0');
            }
            sb.append(Integer.toHexString(b));
        }
        return sb.toString();
    }

    public static long toDec(byte[] bytes) {
        long result = 0;
        long factor = 1;
        for (byte b : bytes) {
            result += (((long) b) & 255) * factor;
            factor *= 256;
        }
        return result;
    }

    private static long toReversedDec(byte[] bytes) {
        long result = 0;
        long factor = 1;
        for (int i = bytes.length - 1; i >= 0; i--) {
            result += (((long) bytes[i]) & 255) * factor;
            factor *= 256;
        }
        return result;
    }

    public static String getServioTagCode(byte[] code){
        int lenghtCode = code.length - 2;
        byte[] apdu1 = new byte[lenghtCode];
        for (int i = 0; i < lenghtCode; i++) {
            apdu1[i] = code[lenghtCode - 1 - i];
        }
        return String.valueOf(Long.parseLong(toHexString(apdu1).replaceAll(" ", ""), 16));
    }

    public static String toHexString(byte[] array) {

        String bufferString = "";

        if (array != null) {
            for (int i = 0; i < array.length; i++) {
                String hexChar = Integer.toHexString(array[i] & 0xFF);
                if (hexChar.length() == 1) {
                    hexChar = "0" + hexChar;
                }
                bufferString += hexChar.toUpperCase(Locale.US) + " ";
            }
        }
        return bufferString;
    }
}
