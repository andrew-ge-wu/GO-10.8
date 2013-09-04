/*************************************************************************************
 * This is an example on how to implement the Polopoly Paywall functionality
 * in vcl (Varnish Configuration Language) that mimics the behaviour in
 * the local authorization setup as can be found in example.paywall.PaywallFilter
 * which also contains setting of the headers used here.
 *
 * --------------------------------------------------------------------------------
 * WARNING! This file is an example only and should not be used directly without
 * proper modification and verification done by experts.
 * --------------------------------------------------------------------------------
 *
 * Note also that the Polopoly Paywall is provided in the Early Access Program and
 * api's, semantics and features may change.
 *
 * We would love to get feedback via pull-requests or on
 * support.polopoly.com to be able to make this vcl example as good possible.
 ***********************************************************************************/
import digest; // https://github.com/varnish/libvmod-digest
import header;
import std;

C{
    #include <string.h>
    #include <stdlib.h>
    #include <limits.h>
    #include <syslog.h>
    #include <time.h>
    #include <stdio.h>

    void TIM_format(double t, char *p);
    double TIM_real(void);

    /**
     * This function will return a non-zero value if the user owns at least one of the
     * required products.
     */
    int search_user_products(const char* user_products, const char* required_products)
    {
        char *usr_buf, *req_buf;
        char* required_products_copy = strdup(required_products);
        char *required_product = strtok_r(required_products_copy, ":", &req_buf);
        int found = 0;
        while(!found && required_product) {
            char *user_products_copy = strdup(user_products);
            char *user_product = strtok_r(user_products_copy, ":", &usr_buf);
            while(!found && user_product) {
                found = strcmp(user_product, required_product) == 0;
                user_product = strtok_r(NULL, ":", &usr_buf);
            }
            free(user_products_copy);
            required_product = strtok_r(NULL, ":", &req_buf);
        }
        free(required_products_copy);

        return found;
    }

    /**
     * This method will grant metered access to the requested article, adding the
     * article id to the list of visited articles and setting the
     * X-Paywall-Is-Authorized header to "true".
     */
    void grant_metered_access(void* sp, const char* articles, const char* timestamp, const char* limit_string, const char* requested_article) {
        int period = strtol(VRT_GetHdr(sp, HDR_RESP, "\021X-Metered-Period:"), NULL, 10);
        int period_seconds = 24 * 60 * 60 * period;
        char buf[40];
        if(timestamp == NULL || strncmp(timestamp, "", 1) == 0) {
            TIM_format(TIM_real() + period_seconds, buf);
            VRT_SetHdr(sp, HDR_RESP, "\014X-Timestamp:", buf, vrt_magic_string_end);
            sprintf(buf, "%ld", (long) time(NULL) + period_seconds);
            timestamp = buf;
        }

        int cookie_length = strlen(timestamp) + 1 + strlen(limit_string) + 1 + 1 + strlen(requested_article);
        if(articles != NULL) {
            cookie_length += strlen(articles);
        }

        char *new_cookie_value = (char*)malloc(cookie_length + 1);
        strcpy(new_cookie_value, timestamp);
        strcat(new_cookie_value, "|");
        strcat(new_cookie_value, limit_string);
        strcat(new_cookie_value, "|");

        if(articles != NULL && strlen(articles) > 0 ) {
            strcat(new_cookie_value, articles);
            strcat(new_cookie_value, ":");
        }
        strcat(new_cookie_value, requested_article);

        VRT_SetHdr(sp, HDR_RESP, "\033X-New-Metered-Cookie-Value:", new_cookie_value, vrt_magic_string_end);
        VRT_SetHdr(sp, HDR_RESP, "\030X-Paywall-Is-Authorized:", "true", vrt_magic_string_end);
        free(new_cookie_value);
    }

}C

backend default {
    .host = "localhost";
    .port = "8080";
}

sub strip_headers {
    unset resp.http.X-User-Session-Key;
    unset resp.http.X-User-Digest;
    unset resp.http.X-Calculated-Digest;
    unset resp.http.X-Crypto-Message;
    unset resp.http.X-User-Valid-Digest;
    unset resp.http.X-Premium-Packages;
    unset resp.http.X-Premium-Redirect-URL;
    unset resp.http.X-Paywall-User-Products;
    unset resp.http.X-Paywall-Is-Authorized;
    unset resp.http.X-Premium-Redirect-Article-URL;
}

sub prepare_paywall_headers {
    set resp.http.X-Paywall-User-Products = regsub(req.http.Cookie, "^.*p_onlineaccess=", "");
    set resp.http.X-Paywall-User-Products = regsub(resp.http.X-Paywall-User-Products, ";.*", "");

    set resp.http.X-User-Session-Key = regsub(req.http.Cookie, {"^.*sessionKey="?"}, "");
    set resp.http.X-User-Session-Key = regsub(resp.http.X-User-Session-Key, {""?;.*"}, "");
    set resp.http.X-User-Session-Key = {"""} + resp.http.X-User-Session-Key + {"""};

    set resp.http.X-User-Digest = regsub(req.http.Cookie, "^.*p_onlineaccess_digest=", "");
    set resp.http.X-User-Digest = regsub(resp.http.X-User-Digest, ";.*", "");

    set resp.http.X-Crypto-Message = resp.http.X-Paywall-User-Products + ":" + resp.http.X-User-Session-Key + ":" + "theSecret";

    set resp.http.X-Calculated-Digest = digest.hash_sha256(resp.http.X-Crypto-Message);

    if (resp.http.X-User-Digest == resp.http.X-Calculated-Digest) {
        set resp.http.X-User-Valid-Digest = "true";
    }
}

sub hard_paywall {
    if(resp.http.X-User-Valid-Digest == "true") {
        C{
            char *user_products = VRT_GetHdr(sp, HDR_RESP, "\030X-Paywall-User-Products:");
            char *required_products = VRT_GetHdr(sp, HDR_RESP, "\023X-Premium-Packages:");

            if(search_user_products(user_products, required_products)) {
                VRT_SetHdr(sp, HDR_RESP, "\030X-Paywall-Is-Authorized:", "true", vrt_magic_string_end);
            }
        }C
    }
}

sub metered_paywall {
    if(req.http.Cookie ~ "p_metered_access") {
        set resp.http.X-Metered-Cookie-Value = regsub(req.http.Cookie, "^.*p_metered_access=", "");
        set resp.http.X-Metered-Cookie-Value = regsub(resp.http.X-Metered-Cookie-Value, ";.*", "");
    } else {
        set resp.http.X-Metered-Cookie-Value = "";
    }
    set resp.http.X-Requested-Article-Id = regsub(resp.http.X-Premium-Redirect-URL, "^.*aId=", "");

    C{
        int found = 0;
        char *cookie_buf, *article_buf;

        char *limit_string = VRT_GetHdr(sp, HDR_RESP, "\020X-Metered-Limit:");
        int limit = strtol(limit_string, NULL, 10);
        char *cookie_copy = strdup(VRT_GetHdr(sp, HDR_RESP, "\027X-Metered-Cookie-Value:"));
        char *timestamp = strtok_r(cookie_copy, "|", &cookie_buf);
        strtok_r(NULL, "|", &cookie_buf);
        char *articles_array = strtok_r(NULL, "|", &cookie_buf);

        char *articles_array_copy;
        if(articles_array != NULL) {
            articles_array_copy = strdup(articles_array);
        } else {
            articles_array_copy = strdup("");
        }

        char *requested_article = VRT_GetHdr(sp, HDR_RESP, "\027X-Requested-Article-Id:");

        char *article = strtok_r(articles_array_copy, ":", &article_buf);

        int remaining_metered_articles = limit;
        while(!found && article != NULL) {
            remaining_metered_articles--;

            found = (strcmp(article, requested_article) == 0);
            article = strtok_r(NULL, ":", &article_buf);
        }

        if(!found && (remaining_metered_articles > 0)) {
            grant_metered_access(sp, articles_array, timestamp, limit_string, requested_article);
        } else if(found) {
            VRT_SetHdr(sp, HDR_RESP, "\030X-Paywall-Is-Authorized:", "true", vrt_magic_string_end);
        }

        free(articles_array_copy);
        free(cookie_copy);
    }C

    if(resp.http.X-New-Metered-Cookie-Value) {
        header.append(resp.http.Set-Cookie,"p_metered_access=" + resp.http.X-New-Metered-Cookie-Value + "; path=/; expires=" + resp.http.X-Timestamp + ";");
    }

}

sub vcl_deliver {
    if(req.http.User-Agent !~ "(?i)googlebot|bingbot|yahoo! slurp" && req.http.Referer !~ "(?i)^https?://(www\.google|www\.bing|.*search.yahoo)\..{2,6}/.*$") {
        if(req.restarts == 0 && resp.http.X-Premium-Packages) {
            call prepare_paywall_headers;
            call hard_paywall;

            if(resp.http.X-Metered-Limit && resp.http.X-Paywall-Is-Authorized != "true") {
                call metered_paywall;
            }

            if(resp.http.X-Paywall-Is-Authorized != "true" && resp.http.X-Premium-Redirect-URL) {
                set req.http.X-Not-Authorized = "true";
                set req.http.X-Premium-Redirect-URL = resp.http.X-Premium-Redirect-URL;
                return(restart);
            }

            if(resp.http.X-Paywall-Is-Authorized == "true" && resp.http.X-Premium-Redirect-Article-URL) {
                set req.http.X-Please-Pay-Redirect = resp.http.X-Premium-Redirect-Article-URL;
                return(restart);
            }
        }
    }

    call strip_headers;
}

sub vcl_fetch {
    set beresp.do_esi = true;
}

sub vcl_recv {
    # Exclude Status servlet
    if (req.url ~ "status") {
        return (pass);
    }
    if (req.http.X-Not-Authorized == "true") {
        error 750 "Not auth";
    }
    if(req.http.X-Please-Pay-Redirect) {
        error 850 "Please pay article redirect";
    }
}

sub vcl_error {
    if (obj.status == 750) {
        set obj.http.Location = req.http.X-Premium-Redirect-URL;
        set obj.status = 302;
        synthetic {""};
        return(deliver);
    }
    if(obj.status == 850) {
        set obj.http.Location = req.http.X-Please-Pay-Redirect;
        set obj.status = 302;
        synthetic {""};
        return(deliver);
    }
}
