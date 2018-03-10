package models;

import play.Logger;
import play.libs.F;
import java.util.concurrent.ConcurrentHashMap;

public class TemporaryStorage {

    private ConcurrentHashMap<String, F.Tuple<User, Long>> map;
    private final static Logger.ALogger LOGGER = Logger.of(TemporaryStorage.class);

    public TemporaryStorage(){
        map = new ConcurrentHashMap<>();
    }


    public void addMap(String token, F.Tuple tuple) {

        map.put(token, tuple);
    }

    public ConcurrentHashMap<String, F.Tuple<User, Long>> getMap() {

        return map;
    }

}
