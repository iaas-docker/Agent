package persistence;

import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.WriteResult;
import io.github.cdimascio.dotenv.Dotenv;
import models.IdObject;
import org.bson.types.ObjectId;
import org.jongo.Jongo;
import org.jongo.MongoCollection;
import util.EPJson;

public class CrudService<T extends IdObject> {

    public final static Dotenv dotenv = Dotenv.load();
    private static DB db = new MongoClient(new MongoClientURI(dotenv.get("MORGO_URL"))).getDB("heroku_35k6mstx");
    private static Jongo jongo = new Jongo(db);

    private Class<T> clazz;
    private String collectionName;
    private MongoCollection collection;


    public CrudService(String collectionName, Class<T> clazz) {
        this.collectionName = collectionName;
        this.clazz = clazz;
    }

    /**
     * This method allows to get a reference to the encapsulated Jongo collection. You can use it
     * to get such a reference and make use of all the Mongo querying capabilities.
     * @return {@link MongoCollection Refrence} to encapsulated Jongo collection
     */
    public MongoCollection collection() {
        if (collection == null)
            collection = jongo.getCollection(collectionName);
        return collection;
    }

    /**
     * Saves an object to DB as a new document.
     * @param object Object to save
     * @return The object
     */
    public T create(T object) {
        object.setId(null);
        WriteResult res = collection().save(object);
        if (res.getUpsertedId() != null) {
            object.setId(res.getUpsertedId().toString());
            return object;
        }
        throw new RuntimeException("Entity " + clazz.getSimpleName() + " cannot be created :(");
    }

    /**
     * Tries to retrieve an object with DB by its Id
     * @param id Id to look for
     * @return Object found or null if not found
     */
    public T findById(String id) {
        return collection().findOne( new ObjectId(id) ).as(clazz);
    }

    /**
     * Saves the object to te DB. This operation performs an upsert depending on whether the object
     * has an id or not. If the creation/update fails, an exception is thrown.
     *
     * @param object Object to be saved
     * @return The same object received as param. The id might have been updated if the operation resulted
     * in a creation.
     * @throws Exception If the operation fails
     */
    public T save(T object) {
        if (object.getId() != null)
            return update(object.getId(), object);
        return create(object);
    }

    public T update(String id, T object) {
        object.setId(id);
        WriteResult res = collection().update(new ObjectId(id)).with(object);
        if (res.getN() > 0)
            return object;
        throw new RuntimeException("Entity " + clazz.getSimpleName() + " cannot be updated :(");
    }
    /**
     * Sets the object defined by id as deleted
     *
     * @param id Id to look for
     */
    public void delete(String id) throws Exception{
        T t = findById(id);
        collection().remove(new ObjectId(id));
    }

    public int hardDelete(Object... query) {
        String deleteQuery = EPJson.string(query);
        System.out.println("deleteQuery = " + deleteQuery);
        WriteResult res = collection().remove(deleteQuery);
        return res.getN();
    }

    public static boolean testConnection(){
        return db.collectionExists("users");
    }
}
