package controllers.Security;

import com.google.inject.Inject;
import daos.UserDao;
import models.User;
import play.Logger;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class AuthenticatorAction extends Action.Simple {

    private final static Logger.ALogger LOGGER = Logger.of(AuthenticatorAction.class);

    private UserDao userDao;

    @Inject
    public AuthenticatorAction(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public CompletionStage<Result> call(Http.Context ctx) {

        LOGGER.debug("AuthenticatorAction2");

        // Get authorization header
        final Optional<String> header = ctx.request().header("Authorization");
        LOGGER.debug("Header: {}", header);
        if (!header.isPresent()) {
            return CompletableFuture.completedFuture(unauthorized());
        }

        // Extract access_token
        if (!header.get().startsWith("Bearer ")) {
            return CompletableFuture.completedFuture(unauthorized());
        }

        final String token = String.valueOf(header.get().substring(7));
        if (null == token) {
            return CompletableFuture.completedFuture(unauthorized());
        }
        LOGGER.debug("Access token: {}", token);

        // Get user by access token from database
        final User user = userDao.findByToken(token);
        if (null == user) {
            return CompletableFuture.completedFuture(unauthorized());
        }
        /*if (token.equals(user.getToken()))
        {
            final JsonNode jsonNode = Json.toJson(user);
        }
        else return  CompletableFuture.completedFuture(unauthorized());
        */
        // Check expiration

        // TODO

        LOGGER.debug("User: {}", user);

        ctx.args.put("user", user);

        return delegate.call(ctx);
    }

}