package tillerino.tillerinobot;


import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.meta.TypeQualifier;

import org.tillerino.osuApiModel.OsuApiBeatmap;
import org.tillerino.osuApiModel.OsuApiUser;
import org.tillerino.osuApiModel.types.BeatmapId;
import org.tillerino.osuApiModel.types.BitwiseMods;
import org.tillerino.osuApiModel.types.UserId;

import tillerino.tillerinobot.BeatmapMeta.PercentageEstimates;
import tillerino.tillerinobot.RecommendationsManager.BareRecommendation;
import tillerino.tillerinobot.RecommendationsManager.GivenRecommendation;
import tillerino.tillerinobot.RecommendationsManager.Model;
import tillerino.tillerinobot.lang.Language;

public interface BotBackend {
	@TypeQualifier(applicableTo = String.class)
	@Retention(RetentionPolicy.RUNTIME)
	public @interface IRCName {

	}

	/**
	 * @param beatmapid
	 * @param mods
	 *            mods for {@link PercentageEstimates}. These might be ignored
	 *            if they can't be satisfied
	 * @param lang
	 *            TODO
	 * @return null if not found
	 * @throws SQLException
	 * @throws IOException
	 * @throws UserException
	 * @throws InterruptedException 
	 */
	@CheckForNull
	public BeatmapMeta loadBeatmap(@BeatmapId int beatmapid, @BitwiseMods long mods, Language lang) throws SQLException, IOException, UserException, InterruptedException;

	public void saveGivenRecommendation(@UserId int userid, @BeatmapId int beatmapid, @BitwiseMods long mods) throws SQLException;

	/**
	 * @param nick
	 * @return the last version of the bot that was visited by this user. -1 if
	 *         no information available.
	 * @throws SQLException
	 * @throws UserException
	 */
	public int getLastVisitedVersion(@Nonnull @IRCName String nick) throws SQLException, UserException;
	
	/**
	 * recommendations from the last two weeks
	 * @param userid
	 * @return ordered by date given from newest to oldest
	 * @throws SQLException
	 */
	List<GivenRecommendation> loadGivenRecommendations(@UserId int userid) throws SQLException;

	public void setLastVisitedVersion(@Nonnull @IRCName String nick, int version) throws SQLException;

	/**
	 * get a user's information
	 * @param userid user id
	 * @param maxAge maximum age of the information. if <= 0 any cached information, if available, will be returned
	 * @return null if the user can't be found
	 * @throws SQLException
	 * @throws IOException API exception
	 */
	@CheckForNull
	public OsuApiUser getUser(@UserId int userid, long maxAge) throws SQLException, IOException;
	
	public void registerActivity(@UserId int userid) throws SQLException;
	
	public long getLastActivity(@Nonnull OsuApiUser user) throws SQLException;

	public int getDonator(@Nonnull OsuApiUser user) throws SQLException, IOException;
	
	/**
	 * resolve an IRC username
	 * @param ircName
	 * @return null if the name could not be resolved
	 * @throws SQLException
	 * @throws IOException API exception
	 */
	@CheckForNull @UserId
	public Integer resolveIRCName(@IRCName @Nonnull String ircName)
			throws SQLException, IOException;

	/**
	 * will load a sampler
	 * @param userid
	 * @param exclude these maps will be excluded (give top50 and previously given recommendations)
	 * @param model selected model
	 * @param nomod don't recommend mods
	 * @param requestMods request specific mods (these will be included, but this won't exclude other mods)
	 * @return
	 * @throws SQLException
	 * @throws IOException
	 * @throws UserException
	 */
	public Collection<BareRecommendation> loadRecommendations(@UserId int userid, @Nonnull Collection<Integer> exclude,
			@Nonnull Model model, boolean nomod, @BitwiseMods long requestMods) throws SQLException, IOException, UserException;
	
	/**
	 * verifies a key for general data queries
	 * @param key
	 * @return
	 * @throws SQLException
	 */
	public boolean verifyGeneralKey(String key) throws SQLException;
	
	/**
	 * retreives options for this user as saved through the
	 * {@link #saveOptions(int, String)} method.
	 * 
	 * @param user
	 * @return may be null or empty string.
	 * @throws SQLException
	 */
	@CheckForNull
	public String getOptions(@UserId int user) throws SQLException;
	
	/**
	 * saves options for this user. options should be saved in a human-readable
	 * format. care must be taken to keep the format backwards-compatible at all
	 * times.
	 * 
	 * @param user
	 * @param options
	 * @throws SQLException
	 */
	public void saveOptions(@UserId int user, String options) throws SQLException;

	/**
	 * forgets all given recommendations of the past for a single user
	 * 
	 * @param user
	 * @throws SQLException
	 */
	public void forgetRecommendations(@UserId int user) throws SQLException;

	/**
	 * Retreives beatmap. Implementation hint: this might be called a *lot* when
	 * checking recommendation predicates and should probably be cached.
	 * 
	 * @param beatmapId
	 * @return null if not found
	 * @throws IOException
	 * @throws SQLException
	 */
	public @CheckForNull OsuApiBeatmap getBeatmap(@BeatmapId int beatmapId) throws SQLException, IOException;
	
	/**
	 * links the given user to a ppaddict account using a token string.
	 * 
	 * @param token a token that was given to the user by the ppaddict website.
	 * @return the name of the ppaddict account that current user was linked to, or null if the token was not valid
	 * @throws SQLException
	 */
	@CheckForNull
	public String tryLinkToPpaddict(String token, OsuApiUser user) throws SQLException;
}
