package com.pf.mr;

import java.util.List;

/**
 * Created by magnushyttsten on 3/26/16.
 */
public class QL_Set {

    public long access_type;
    public String can_edit;
    public String created_by;
    public String created_date;

    public QL_Creator creator;

    public long creator_id;
    public String description;
    public String display_timestamp;
    public String editable;
    public String has_access;
    public String has_images;
    public long id;
    public String lang_definitions;
    public String lang_terms;
    public String modified_date;
    public long password_edit;
    public long password_use;
    public String published_date;
    public long term_count;

    public List<QL_Term> terms;

    public String title;
    public String url;
    public String visibility;
}
