package mflix.api.daos;

import static com.mongodb.client.model.Updates.set;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

import java.util.Map;

import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;

import mflix.api.models.Session;
import mflix.api.models.User;

@Configuration
public class UserDao extends AbstractMFlixDao {

    private final MongoCollection<User> usersCollection;
    //TODO> Ticket: User Management - do the necessary changes so that the sessions collection
    //returns a Session object
    private final MongoCollection<Document> sessionsCollection;

    private final Logger log;

    @Autowired
    public UserDao(
            MongoClient mongoClient, @Value("${spring.mongodb.database}") String databaseName) {
        super(mongoClient, databaseName);
        CodecRegistry pojoCodecRegistry =
                fromRegistries(
                        MongoClientSettings.getDefaultCodecRegistry(),
                        fromProviders(PojoCodecProvider.builder().automatic(true).build()));

        usersCollection = db.getCollection("users", User.class).withCodecRegistry(pojoCodecRegistry);
        log = LoggerFactory.getLogger(this.getClass());
        //TODO> Ticket: User Management - implement the necessary changes so that the sessions
        // collection returns a Session objects instead of Document objects.
        sessionsCollection = db.getCollection("sessions");
    }

    /**
     * Inserts the `user` object in the `users` collection.
     *
     * @param user - User object to be added
     * @return True if successful, throw IncorrectDaoOperation otherwise
     */
    public boolean addUser(User user) {
        //TODO > Ticket: Durable Writes -  you might want to use a more durable write concern here!
        try {
        	usersCollection.insertOne(user);
        }catch(Exception ex) {
        	throw new IncorrectDaoOperation("User already exist!");
        }
        return true;
        //TODO > Ticket: Handling Errors - make sure to only add new users
        // and not users that already exist.

    }

    /**
     * Creates session using userId and jwt token.
     *
     * @param userId - user string identifier
     * @param jwt    - jwt string token
     * @return true if successful
     */
    public boolean createUserSession(String userId, String jwt) {
        //TODO> Ticket: User Management - implement the method that allows session information to be
        // stored in it's designated collection.
    	Document session = new Document("user_id",userId);
    	session.put("jwt", jwt);
    	try {
    		if(getUser(userId) != null) {
    			sessionsCollection.findOneAndDelete(new Document("user_id", userId));
    		}
	    	sessionsCollection.insertOne(session); 
    	}catch(Exception e) {
    		throw new IncorrectDaoOperation("No such user_id available!");
    	} 
    	Document retrieved = sessionsCollection.find(Filters.eq("_id", session.getObjectId("_id"))).first();
          
    	if(retrieved != null && retrieved.getString("jwt").equals(jwt))
    		return true;
    	else
    		return false;
        //TODO > Ticket: Handling Errors - implement a safeguard against
        // creating a session with the same jwt token.
    	
    	/* Approved solution */
	    	/*Bson updateFilter = new Document("user_id", userId);
	        Bson setUpdate = Updates.set("jwt", jwt);
	        UpdateOptions options = new UpdateOptions().upsert(true);
	        sessionsCollection.updateOne(updateFilter, setUpdate, options);
	        return true;*/	
    	/* Approved solution */
    }

    /**
     * Returns the User object matching the an email string value.
     *
     * @param email - email string to be matched.
     * @return User object or null.
     */
    public User getUser(String email) {
        User user = null;
        //TODO> Ticket: User Management - implement the query that returns the first User object.
        Bson filter = Filters.eq("email", email);
        user = usersCollection.find(filter).first();
        return user;
        
        /* Approved solution */
        	//return usersCollection.find(new Document("email", email)).limit(1).first();
        /* Approved solution */
    }

    /**
     * Given the userId, returns a Session object.
     *
     * @param userId - user string identifier.
     * @return Session object or null.
     */
    public Session getUserSession(String userId) {
        //TODO> Ticket: User Management - implement the method that returns Sessions for a given
        // userId
    	Document document = null;
    	Bson filter = Filters.eq("user_id", userId);
    	document = sessionsCollection.find(filter).first();
    	
    	Session session = null;
    	if(document != null) {
    		session = new Session();
    		session.setUserId(document.getString("user_id"));
    		session.setJwt(document.getString("jwt"));
    	}
    	
        return session;
        
        /* Approved solution */
        	//return sessionsCollection.find(new Document("user_id", userId)).limit(1).first();
        /* Approved solution */
    }

    public boolean deleteUserSessions(String userId) {
        //TODO> Ticket: User Management - implement the delete user sessions method
    	Bson query = new Document("user_id", userId);
    	DeleteResult deleteSessions = sessionsCollection.deleteMany(query);
    	if(deleteSessions.wasAcknowledged())
    		return true;
    	else
    		return false; 
    	
    	/* Approved solution */
    		/*  Document sessionDeleteFilter = new Document("user_id", userId);
			    DeleteResult res = sessionsCollection.deleteOne(sessionDeleteFilter);
			    if (res.getDeletedCount() < 1) {
			      log.warn("User `{}` could not be found in sessions collection.", userId);
			      }
			
			      return res.wasAcknowledged();
			*/
    	/* Approved solution */    	
    }

    /**
     * Removes the user document that match the provided email.
     *
     * @param email - of the user to be deleted.
     * @return true if user successfully removed
     */
    public boolean deleteUser(String email) {
        // remove user sessions
        //TODO> Ticket: User Management - implement the delete user method
        //TODO > Ticket: Handling Errors - make this method more robust by
        // handling potential exceptions.
    	if(getUserSession(email) != null) {
    		deleteUserSessions(email);
    	}
    	Bson query = new Document("email", email);
    	User deleteUser = null;
    	try {
    		deleteUser = usersCollection.findOneAndDelete(query);
    	}catch(Exception ex) {
    		throw new IncorrectDaoOperation("No such user found!");
    	}
    	if(deleteUser != null)
    		return true;
    	else
    		return false; 
    	
    	/* Approved Solution */
	    	/*// remove user sessions
	        if (deleteUserSessions(email)) {
	          Document userDeleteFilter = new Document("email", email);
	          DeleteResult res = usersCollection.deleteOne(userDeleteFilter);
	
	          if (res.getDeletedCount() < 0) {
	            log.warn("User with `email` {} not found. Potential concurrent operation?!");
	          }
	
	          return res.wasAcknowledged();
	        }
	        return false;*/
    	/* Approved Solution */
    }

    /**
     * Updates the preferences of an user identified by `email` parameter.
     *
     * @param email           - user to be updated email
     * @param userPreferences - set of preferences that should be stored and replace the existing
     *                        ones. Cannot be set to null value
     * @return User object that just been updated.
     */
    public boolean updateUserPreferences(String email, Map<String, ?> userPreferences) {
        //TODO> Ticket: User Preferences - implement the method that allows for user preferences to
        // be updated.
        //TODO > Ticket: Handling Errors - make this method more robust by
        // handling potential exceptions when updating an entry.
    	
		if(userPreferences == null)
			throw new IncorrectDaoOperation("Prefrences can not be null!"); 
    	
    	Bson query = new Document("email", email); 
    	UpdateResult result = null;
    	try {
    		result = usersCollection.updateOne(query, set("preferences", userPreferences));
    	}catch(Exception ex) {
    		throw new IncorrectDaoOperation("No Such user available!");
    	}
    	if(result.getModifiedCount() > 0)
    		return true;
    	else
    		return false;
    	
    	/* Approved Solution */
	    	/*// make sure to check if userPreferences are not null. If null, return false immediately.
	        if(userPreferences == null){
	            throw new IncorrectDaoOperation(
	              "userPreferences cannot be set to null");
	        }
	        // create query filter and update object.
	        Bson updateFilter = new Document("email", email);
	        Bson updateObject = Updates.set("preferences", userPreferences);
	        // update one document matching email.
	        UpdateResult res = usersCollection.updateOne(updateFilter, updateObject);
	        if(res.getModifiedCount() < 1){
	            log.warn("User `{}` was not updated. Trying to re-write the same `preferences` field: `{}`",
	                    email, userPreferences);
	        }
	        return true;*/
    	/* Approved Solution */
    }
}
