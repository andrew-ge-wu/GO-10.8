/*
 * (c) Polopoly AB (publ).
 * This software is protected by copyright law and international copyright
 * treaties as well as other intellectual property laws and treaties.
 * All title and rights in and to this software and any copies thereof
 * are the sole property of Polopoly AB (publ).
 * Polopoly is a registered trademark of Polopoly AB (publ).
 */

package com.atex.plugins.brightcove.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.atex.plugins.brightcove.BrightcoveVideoPolicy;
import com.atex.plugins.brightcove.service.BrightcoveService;
import com.brightcove.mediaapi.exceptions.BrightcoveException;
import com.brightcove.org.json.JSONException;
import com.polopoly.cm.client.CMException;

public class BrightCoveVideoUploader {

    private static final Logger LOGGER = Logger.getLogger(BrightCoveVideoUploader.class.getName());

    private String writeToken;
    private String siteId;

    public BrightCoveVideoUploader(String writeToken, String siteId) {
        this.writeToken = writeToken;
        this.siteId = siteId;
    }

    /**
     * @param contentPolicy ContentPolicy that hold meta information of the video
     * @param is Video InputStream to be uploaded
     * @param writeToken Brightcove Write Token used 
     * @param fileName Filename of the file to be uploaded
     * @return Brightcove video id upon successful upload
     * @throws BrightcoveException
     */
    public String upload(BrightcoveVideoPolicy contentPolicy, InputStream is, String writeToken, String fileName, Map<String, String> mapping) throws BrightcoveException {        
        try {
            JSONObject json = genCreateVideoJson(contentPolicy, mapping);
            return extractBcVideoId(getBrightcoveService().addVideo(json.toString(), is, fileName));
        } catch (CMException e) {
            LOGGER.log(Level.WARNING, "Unable to read info from CM");
            return "";
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "IO issue");
            return "";
        }
    }

    @SuppressWarnings("unchecked")
    private JSONObject genCreateVideoJson(BrightcoveVideoPolicy contentPolicy, Map<String, String> mapping) throws CMException, BrightcoveException {
        try {
            JSONObject params = new JSONObject();
            params.put("token", writeToken);
            params.put("video", contentPolicy.toVideo(mapping).toJson());
            JSONObject main = new JSONObject();
            main.put("method", "create_video");
            main.put("params", params);
            return main;
        } catch (JSONException e) {
            throw new BrightcoveException(e);
        }
    }

    private String extractBcVideoId(String json) throws BrightcoveException {
        JSONParser parser = new JSONParser();
        JSONObject obj;
        Long result;
        JSONObject error;
        String message;
        try {
            obj = (JSONObject) parser.parse(json);
            result = (Long) obj.get("result");
            if (result==null) {
                LOGGER.log(Level.INFO, "From Brightcove :" + obj.toJSONString());
                error =  (JSONObject) obj.get("error");
                message = (String) error.get("message");
                throw new BrightcoveException(message);
            }
        } catch (ParseException e) {
            LOGGER.log(Level.WARNING, "Error occur when parsing callback json");
            return "";
        }
        return String.valueOf(result);
    }

    public BrightcoveService getBrightcoveService() {
        return new BrightcoveService(null, siteId);
    }
}
