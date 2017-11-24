package com.pf.mr.be.datamodel;

import java.util.List;

/**
 * Created by magnushyttsten on 3/26/16.
 */
public class QLSet {

    public String[] subjects; // newone
    public String[] class_ids; // newone

    public long access_type;
    public boolean can_edit;
    public String created_by; //x
    public long created_date;

    public QLCreator creator; //x

    public long creator_id;
    public String description;
    public Object display_timestamp;
    public String editable;
    public boolean has_access;
    public boolean has_images;
    public long id;
    public String lang_definitions;
    public String lang_terms;
    public long modified_date; //x
    public long password_edit;
    public long password_use;
    public long published_date; // was String
    public long term_count;

    public List<QLTerm> terms;

    public String title;
    public String url;
    public String visibility;
}
