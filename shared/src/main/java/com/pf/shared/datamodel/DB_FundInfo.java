package com.pf.shared.datamodel;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map;
import java.util.logging.Logger;

public class DB_FundInfo {
    private static final Logger log = Logger.getLogger(DB_FundInfo.class.getName());

    public static boolean _isInitialized;
    private static List<D_FundInfo> _fis;
    private static Map<String, List<D_FundInfo>> _fisByTypeHM;
    private static List<String> _indexes;
    private static Map<String, List<D_FundInfo>> _fisByIndexHM;

    //------------------------------------------------------------------------
    public static void initialize(byte[] fundInfoData) throws IOException {
        log.info("D_FundInfo.initialize");
        if (_isInitialized) {
            log.info("...already initialized, returning");
            return;
        }

        log.info("...decrunching and rebuilding again");
        _fis = D_FundInfo_Serializer.decrunchFundList(fundInfoData);
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

    // ***********************************************************************

    //------------------------------------------------------------------------
    public static byte[] addFundInfo(D_FundInfo fiAdd) throws Exception {
        for (D_FundInfo fi: _fis) {
            if (fi.getTypeAndName().equals(fiAdd.getTypeAndName())) {
                throw new IOException("Cannot add fund already existing: " + fi.getTypeAndName() + ", attempted to add: " + fiAdd.getTypeAndName());
            }
        }
        _fis.add(fiAdd);
        _fis.sort(new Comparator<D_FundInfo>() {
            @Override
            public int compare(D_FundInfo o1, D_FundInfo o2) {
                return o1.getTypeAndName().compareTo(o2.getTypeAndName());
            }
        });
        rebuild();
        byte[] data = D_FundInfo_Serializer.crunchFundList(_fis);
        return data;
    }

    //------------------------------------------------------------------------
    public static byte[] deleteFundInfo(D_FundInfo fiDel) throws Exception {
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
        rebuild();
        byte[] data = D_FundInfo_Serializer.crunchFundList(_fis);
        return data;
    }

    //------------------------------------------------------------------------
    public static byte[] getFundInfosData() throws IOException {
        return D_FundInfo_Serializer.crunchFundList(_fis);
    }


    // ***********************************************************************

    //------------------------------------------------------------------------
    public static List<D_FundInfo> getAllFundInfos() {
        return _fis;
    }
    public static List<String> getAllIndexes() { return _indexes; }
    public static List<D_FundInfo> getFundInfosByIndex(String index) { return _fisByIndexHM.get(index); }

    //------------------------------------------------------------------------
    public static List<D_FundInfo> getFundInfosByType(String type) {
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
            List<D_FundInfo> fis = _fisByTypeHM.get(type);
            sortFundInfos(fis);
            return fis;
        }

        log.info("getFundsByType, returning all funds: " + _fis.size());
        sortFundInfos(_fis);
        return _fis;
    }

    //------------------------------------------------------------------------
    private static void sortFundInfos(List<D_FundInfo> fis) {
        fis.sort(new Comparator<D_FundInfo>() {
            @Override
            public int compare(D_FundInfo o1, D_FundInfo o2) {
                return o1._nameMS.compareTo(o2._nameMS);
            }
        });
    }

    //------------------------------------------------------------------------
    public static D_FundInfo getFundInfosByTypeAndName(String type, String name) throws IOException {
        List<D_FundInfo> l = _fisByTypeHM.get(type);
        for (D_FundInfo fi: l) {
            if (fi._nameMS.equals(name)) {
                return fi;
            }
        }
        throw new IOException("Did not find name: " + type + "." + name);
    }

    //------------------------------------------------------------------------
    public static D_FundInfo getFundInfosByTypeAndURL(String type, String url) throws IOException {
        List<D_FundInfo> l = _fisByTypeHM.get(type);
        for (D_FundInfo fi: l) {
            if (fi._url.equals(url)) {
                return fi;
            }
        }
        throw new IOException("Did not find name: " + type + ", and url: " + url);
    }

}
