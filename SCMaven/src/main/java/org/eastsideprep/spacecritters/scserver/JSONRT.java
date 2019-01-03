
package org.eastsideprep.spacecritters.scserver;

import com.google.gson.*;
import spark.*;

/**
 *
 * @author gmein
 */
public class JSONRT implements ResponseTransformer {

    final static private Gson gson = new Gson();

    @Override
    public String render(Object o) {
        return gson.toJson(o);
    }

}
