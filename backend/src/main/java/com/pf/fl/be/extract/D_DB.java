package com.pf.fl.be.extract;

import com.pf.fl.be.jsphelper.JSP_Helper;
import com.pf.fl.be.util.EE;
import com.pf.shared.datamodel.D_FundInfo;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import com.pf.shared.Constants;
import com.pf.shared.datamodel.D_FundInfo_Serializer;
import com.pf.shared.utils.Compresser;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import com.google.api.gax.paging.Page;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

public class D_DB {
    private static final Logger log = Logger.getLogger(D_DB.class.getName());

    public static boolean _isInitialized;
    public static List<D_FundInfo> _fis;
    private static Map<String, List<D_FundInfo>> _fisByTypeHM;
    public static List<String> _indexes;
    public static Map<String, List<D_FundInfo>> _fisByIndexHM;

    public static List<D_FundInfo> getFundsByType(String type) {
        if (type == null) {
            return _fis;
        }
        if (type.equals(D_FundInfo.TYPE_INVALID)) {
            List<D_FundInfo> r = new ArrayList<>();
            for (D_FundInfo fi: _fis) {
                if (!fi._isValid) {
                    r.add(fi);
                }
            }
            return r;
        }

        if (type.equals(D_FundInfo.TYPE_ALL)) {
            type = null;
        }
        if (type != null) {
            log.info("getFundsByType, returning funds for type: " + type);
            return _fisByTypeHM.get(type);
        }

        log.info("getFundsByType, returning all funds: " + _fis.size());
        return _fis;
    }

    public static void addAndSaveFundInfo(D_FundInfo fiAdd) throws Exception {
        for (D_FundInfo fi: _fis) {
            if (fi.getTypeAndName().equals(fi.getTypeAndName())) {
                throw new IOException("Cannot add fund already existing: " + fi.getTypeAndName());
            }
        }
        _fis.add(fiAdd);
        _fis.sort(new Comparator<D_FundInfo>() {
            @Override
            public int compare(D_FundInfo o1, D_FundInfo o2) {
                return o1.getTypeAndName().compareTo(o2.getTypeAndName());
            }
        });
        saveFundList(null, _fis, Constants.FUNDINFO_DB_MASTER, false);
    }

    public static void saveFundInfo() throws Exception {
        saveFundList(null, _fis, Constants.FUNDINFO_DB_MASTER, false);
        rebuild();
    }

    public static void deleteFundInfo(D_FundInfo fiDel) throws Exception {
        boolean found = false;
        log.info("*** Deleting fund: " + fiDel.getTypeAndName());
        for (int i=0; i < _fis.size(); i++) {
            D_FundInfo fi = _fis.get(i);
            if (fi.getTypeAndName().equals(fiDel.getTypeAndName())) {
                log.info("Found fund, deleting it");
                found = true;
                _fis.remove(i);
                break;
            }
        }
        if (!found) {
            throw new IOException("Fund " + fiDel.getTypeAndName() + " did not exist, so cannot be deleted");
        }
        log.info("Saving DB again");
        saveFundList(null, _fis, Constants.FUNDINFO_DB_MASTER, false);
    }

    public static void initialize() throws IOException {
        if (_isInitialized) {
            return;
        }

        _fis = readFundList(Constants.FUNDINFO_DB_MASTER);
        _fis.sort(new Comparator<D_FundInfo>() {
            @Override
            public int compare(D_FundInfo o1, D_FundInfo o2) {
                return o1.getTypeAndName().compareTo(o2.getTypeAndName());
            }
        });
        rebuild();
        _isInitialized = true;
    }
    private static void rebuild() throws IOException {
        _fisByTypeHM = new HashMap<>();
        _fisByIndexHM = new HashMap<>();
        _indexes = new ArrayList<>();
        for (D_FundInfo fi: _fis) {

            if (!_fisByTypeHM.containsKey(fi._type)) {
                _fisByTypeHM.put(fi._type, new ArrayList<D_FundInfo>());
            }
            _fisByTypeHM.get(fi._type).add(fi);

            String in = fi._indexName;
            if (in == null || in.trim().length() == 0) {
                in = "-";
            }
            if (!_fisByIndexHM.containsKey(in)) {
                _indexes.add(in);
                _fisByIndexHM.put(in, new ArrayList<D_FundInfo>());
            }
            _fisByIndexHM.get(in).add(fi);
            _indexes.sort(new Comparator<String>() {
                @Override
                public int compare(String o1, String o2) {
                    return o1.compareTo(o2);
                }
            });
        }
    }

    public static D_FundInfo getFundInfoByTypeAndName(String type, String name) throws IOException {
        List<D_FundInfo> l = _fisByTypeHM.get(type);
        for (D_FundInfo fi: l) {
            if (fi._nameMS.equals(name)) {
                return fi;
            }
        }
        throw new IOException("Did not find name: " + type + "." + name);
    }

    public static D_FundInfo getFundInfoByTypeAndURL(String type, String url) throws IOException {
        List<D_FundInfo> l = _fisByTypeHM.get(type);
        for (D_FundInfo fi: l) {
            if (fi._url.equals(url)) {
                return fi;
            }
        }
        throw new IOException("Did not find name: " + type + ", and url: " + url);
    }

    // ******************************************************************

    public static List<D_FundInfo> readFundList(String prefix) throws IOException {
        byte[] data = gcsReadFile(prefix, Constants.EXT_BIN, true);
        return readFundListFromData(data);
    }
    public static List<D_FundInfo> readFundListFromData(byte[] data) throws IOException {
        ByteArrayInputStream bin = new ByteArrayInputStream(data);
        DataInputStream din = new DataInputStream(bin);
        List<D_FundInfo> l = new ArrayList<>();
        while (din.available() > 0) {
            D_FundInfo fi = D_FundInfo_Serializer.decrunch_D_FundInfo(din);
            l.add(fi);
        }
        return l;
    }

    public static void saveFundList(String fridayLastYYMMDD, List<D_FundInfo> l, String prefix, boolean addDate) throws IOException {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(bout);

        for (D_FundInfo fi: l) {
            D_FundInfo_Serializer.crunch_D_FundInfo(dout, fi);
        }

        dout.flush();
        byte[] data = bout.toByteArray();
        gcsWriteFile(fridayLastYYMMDD, prefix, Constants.EXT_BIN, data, addDate, true);
    }

    // ******************************************************************

    public static void gcsWriteFile(String fridayLastYYMMDD, String prefix, String ext, byte[] data, boolean addDate, boolean compress) throws IOException {
        Blob blob = gcsGetBlob(prefix);
        if (blob != null) {
            blob.delete();
        }

        Storage storage = null;
        storage = StorageOptions.getDefaultInstance().getService();
        String fname = null;
        if (addDate) {
            fname = prefix + fridayLastYYMMDD + ext;
        } else {
            fname = prefix + ext;
        }
        try {
            if (compress) {
                data = Compresser.dataCompress(fname, data);
            }
            blob = storage.create(BlobInfo.newBuilder(Constants.BUCKET, fname).build(), data);
        } catch(Exception exc) {
            throw new AssertionError(exc);
        }
    }

    public static byte[] gcsReadFile(String prefix, String ext, boolean decompress) throws IOException {
        Blob blob = null;
        blob = gcsGetBlob(prefix);
        if (blob == null) {
            return null;
        }
//            ee.dinfo(log, TAG, "Reading file: " + blob.getName());
        return gcsReadBlob(blob, decompress);
    }

    public static byte[] gcsReadBlob(Blob blob, boolean decompress) throws IOException {
        byte[] dataBA = blob.getContent();
        if (decompress) {
            dataBA = Compresser.dataUncompress(dataBA);
        }
        return dataBA;
    }

    private static Blob gcsGetBlob(String prefix) throws IOException {
        List<Blob> blobs = gcsGetBlobsInAscendingOrder(prefix);
        if (blobs.size() == 0) {
            return null;
        }
        return blobs.get(0);
    }

    public static List<Blob> gcsGetBlobsInAscendingOrder(String prefix) throws IOException {
        Storage storage = null;
        storage = StorageOptions.getDefaultInstance().getService();

        EE ee = EE.getEE();

        try {
            Bucket bucket = storage.get(Constants.BUCKET);
            Page<Blob> pblob = bucket.list();
            Iterable<Blob> iterator = pblob.iterateAll();
            List<Blob> blobs = new ArrayList<>();
            for (Blob blob : iterator) {
                String bname = blob.getName();
                if (bname != null && bname.startsWith(prefix)) {
                    blobs.add(blob);
                }
            }

            Collections.sort(blobs, new Comparator<Blob>() {
                @Override
                public int compare(Blob o1, Blob o2) {
                    return -o1.getName().compareTo(o2.getName());
                }
            });
            return blobs;
        } catch(Exception exc) {
            throw new AssertionError(exc);
        }
    }
}
