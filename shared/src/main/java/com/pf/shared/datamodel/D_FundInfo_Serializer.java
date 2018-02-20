package com.pf.shared.datamodel;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.logging.Logger;

public class D_FundInfo_Serializer {
    private static final Logger log = Logger.getLogger(D_FundInfo_Serializer.class.getName());

    public static final String TAG_FI_START = "DFIS";
    public static final String TAG_DPD_START = "DPDS";
    public static final String TAG_FI_END = "DFIE";
    public static final String TAG_DPY_START = "DPYS";
    public static void crunch_D_FundInfo(DataOutputStream dout_output, D_FundInfo fi) throws IOException {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(bout);
        dout.writeUTF(fi._url);
        dout.writeInt(fi._errorCode);
        dout.writeUTF(fi._type);
        dout.writeUTF(fi._nameMS);
        dout.writeUTF(fi._nameOrig);
        dout.writeUTF(fi._dateYYMMDD_Updated);
        dout.writeUTF(fi._dateYYMMDD_Update_Attempted);
        dout.writeInt(fi._msRating);
        dout.writeUTF(fi._ppmNumber);
        dout.writeUTF(fi._categoryName);
        dout.writeUTF(fi._indexName);
        dout.writeUTF(fi._currencyName);

        for (D_FundDPYear dpy : fi._dpYears) {
            dout.writeUTF(TAG_DPY_START);
            dout.writeShort(dpy._year);
            dout.writeFloat(dpy._resultFund);
            dout.writeFloat(dpy._resultCategory);
            dout.writeFloat(dpy._resultIndex);
        }

        for (D_FundDPDay dpd : fi._dpDays) {
            dout.writeUTF(TAG_DPD_START);
            dout.writeUTF(dpd._dateYYMMDD);
            dout.writeUTF(dpd._dateYYMMDD_Actual);
            dout.writeUTF(dpd._currency);
            dout.writeFloat(dpd._r1d);
            dout.writeFloat(dpd._r1w);
            dout.writeFloat(dpd._r1m);
            dout.writeFloat(dpd._r3m);
            dout.writeFloat(dpd._r6m);
            dout.writeFloat(dpd._r1y);
            dout.writeFloat(dpd._r3y);
            dout.writeFloat(dpd._r5y);
            dout.writeFloat(dpd._r10y);
            dout.writeFloat(dpd._rYTDFund);
            dout.writeFloat(dpd._rYTDCategory);
            dout.writeFloat(dpd._rYTDIndex);
        }
        dout.writeUTF(TAG_FI_END);

        dout.flush();
        byte[] data = bout.toByteArray();
        dout_output.writeUTF(TAG_FI_START);
        dout_output.writeInt(data.length);
        dout_output.write(data);
    }

    public static D_FundInfo decrunch_D_FundInfo(DataInputStream din_input) throws IOException {
        D_FundInfo fi = new D_FundInfo();

        String tag = din_input.readUTF();
        int length = din_input.readInt();
        byte[] record = new byte[length];
        int rlength = din_input.read(record);
        if (rlength != length) {
            throw new IOException("Not enough bytes to read entire record");
        }

        ByteArrayInputStream bin = new ByteArrayInputStream(record);
        DataInputStream din = new DataInputStream(bin);

        fi._url = din.readUTF();
        fi._errorCode = din.readInt();
        fi._type = din.readUTF();
        fi._nameMS = din.readUTF();
        fi._nameOrig = din.readUTF();
        fi._dateYYMMDD_Updated = din.readUTF();
        fi._dateYYMMDD_Update_Attempted = din.readUTF();
        fi._msRating = din.readInt();
        fi._ppmNumber = din.readUTF();
        fi._categoryName = din.readUTF();
        fi._indexName = din.readUTF();
        fi._currencyName = din.readUTF();

        while (true) {
            tag = din.readUTF();
            if (tag.equals(TAG_FI_END)) {
                break;
            }

            if (tag.equals(TAG_DPY_START)) {
                D_FundDPYear dpy = new D_FundDPYear();
                fi._dpYears.add(dpy);
                dpy._year = din.readShort();
                dpy._resultFund = din.readFloat();
                dpy._resultCategory = din.readFloat();
                dpy._resultIndex = din.readFloat();
            }

            else if (tag.equals(TAG_DPD_START)) {
                D_FundDPDay dpd = new D_FundDPDay();
                fi._dpDays.add(dpd);
                dpd._dateYYMMDD = din.readUTF();
                dpd._dateYYMMDD_Actual = din.readUTF();
                dpd._currency = din.readUTF();
                dpd._r1d = din.readFloat();
                dpd._r1w = din.readFloat();
                dpd._r1m = din.readFloat();
                dpd._r3m = din.readFloat();
                dpd._r6m = din.readFloat();
                dpd._r1y = din.readFloat();
                dpd._r3y = din.readFloat();
                dpd._r5y = din.readFloat();
                dpd._r10y = din.readFloat();
                dpd._rYTDFund = din.readFloat();
                dpd._rYTDCategory = din.readFloat();
                dpd._rYTDIndex = din.readFloat();
            }

            else {
                throw new AssertionError("Unlexpected tag: " + tag + " for fund: " + fi.getTypeAndName());
            }
        }
        assert din.available() == 0: "Did not end with 0 bytes left";
        return fi;
    }

    private static float D2f(Double d) {
        if (d == null) {
            return D_FundDPDay.FLOAT_NULL;
        }
        return (float)d.doubleValue();
    }
}
