package fi.nls.oskari.user;

import fi.nls.oskari.db.DatasourceHelper;
import fi.nls.oskari.domain.Role;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.ontology.service.KeywordMapper;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MybatisUserService {
    private MybatisRoleService roleService = new MybatisRoleService();

    private static final Logger log = LogFactory.getLogger(MybatisUserService.class);

    private SqlSessionFactory factory = null;

    public MybatisUserService() {
        final DatasourceHelper helper = DatasourceHelper.getInstance();
        final DataSource dataSource = helper.getDataSource(helper.getOskariDataSourceName("users"));
        if (dataSource != null) {
            factory = initializeMyBatis(dataSource);
        } else {
            log.error("Couldn't get datasource for usersservice");
        }
    }

    private SqlSessionFactory initializeMyBatis(final DataSource dataSource) {
        final TransactionFactory transactionFactory = new JdbcTransactionFactory();
        final Environment environment = new Environment("development", transactionFactory, dataSource);

        final Configuration configuration = new Configuration(environment);
        configuration.setLazyLoadingEnabled(true);
        configuration.addMapper(KeywordMapper.class);

        return new SqlSessionFactoryBuilder().build(configuration);
    }

    public List<User> findAll(){
        //TODO implement
        return Collections.emptyList();
    }

    public Long addUser(User user) {
        final SqlSession session = factory.openSession();
        long userId = 0;
        try {
            log.debug("Adding user: ", user);
            final UsersMapper mapper = session.getMapper(UsersMapper.class);
            mapper.addUser(user);
            //TODO get user id
            //keywordId =  mapper.addKeyword(keyword);
            //keyword.setId(keywordId);
        } catch (Exception e) {
            log.warn(e, "Exception when trying to add user: ", user);
        } finally {
            session.close();
        }
        log.warn("Got user id:", userId);
        return userId;
    }

    public void updateUser(User user) {
        final SqlSession session = factory.openSession();
        try {
            log.debug("Updating user: ", user);
            final UsersMapper mapper = session.getMapper(UsersMapper.class);
            mapper.updateUser(user);
        } catch (Exception e) {
            log.warn(e, "Exception when trying to update user: ", user);
        } finally {
            session.close();
        }
        log.warn("Got updated user: " + user);
    }

    public User find(long id) {
        final SqlSession session = factory.openSession();
        User user = null;
        try {
            log.debug("Finding user by id: ", id);
            final UsersMapper mapper = session.getMapper(UsersMapper.class);
            user = mapper.find(id);
        } catch (Exception e) {
            log.warn(e, "Exception when trying to find user: ", user);
        } finally {
            session.close();
        }
        log.warn("Found user: " + user);
        return user;
    }

    /**
     * Returns null if doesn't match any user or username for the user that was found
     *
     * @param username
     * @param password
     * @return
     */
    public String login(final String username, final String password) {
        final SqlSession session = factory.openSession();
        String login = null;
        try {
            log.debug("Finding user by username and password: ", username);
            final UsersMapper mapper = session.getMapper(UsersMapper.class);
            Map<String, String> params = new HashMap<String, String>(2);
            params.put("username", username);
            params.put("password", password);
            login = mapper.login(params);
        } catch (Exception e) {
            log.warn(e, "Exception when trying to login with username: ", username);
        } finally {
            session.close();
        }
        log.warn("Found login: " + login);
        return login;
    }

    public String getPassword(final String username) {
        final SqlSession session = factory.openSession();
        String password = null;
        try {
            log.debug("Finding password by username: ", username);
            final UsersMapper mapper = session.getMapper(UsersMapper.class);
            password = mapper.getPassword(username);
        } catch (Exception e) {
            log.warn(e, "Exception when trying to get password for username: ", username);
        } finally {
            session.close();
        }
        log.warn("Found password");
        return password;
    }


    public User findByUserName(String username) {
        final SqlSession session = factory.openSession();
        User user = null;
        try {
            log.debug("Finding user by username: ", username);
            final UsersMapper mapper = session.getMapper(UsersMapper.class);
            user = mapper.findByUserName(username);
        } catch (Exception e) {
            log.warn(e, "Exception when trying to find user by email: ", username);
        } finally {
            session.close();
        }
        log.warn("Found user: " + user);
        loadRoles(user);
        return user;
    }

    public User findByEmail(String email) {
        final SqlSession session = factory.openSession();
        User user = null;
        try {
            log.debug("Finding user by email: ", email);
            final UsersMapper mapper = session.getMapper(UsersMapper.class);
            user = mapper.findByEmail(email.toLowerCase());
        } catch (Exception e) {
            log.warn(e, "Exception when trying to find user by email: ", email);
        } finally {
            session.close();
        }
        log.warn("Found user: " + user);
        loadRoles(user);
        return user;
    }

    private void loadRoles(User user) {
        if (user == null) {
            return;
        }
        List<Role> roleList = roleService.findByUserId(user.getId());
        for (Role role : roleList) {
            user.addRole(role.getId(), role.getName());
        }
    }

    public void delete(long id) {
        final SqlSession session = factory.openSession();
        try {
            log.debug("Deleting user by id: ", id);
            final UsersMapper mapper = session.getMapper(UsersMapper.class);
            mapper.delete(id);
        } catch (Exception e) {
            log.warn(e, "Exception when trying to delete user by id: ", id);
        } finally {
            session.close();
        }
        log.warn("Deleted user with id: " + id);
    }

    public void setPassword(String username, String password) {
        final SqlSession session = factory.openSession();
        try {
            log.debug("Setting password to user: ", username);
            final UsersMapper mapper = session.getMapper(UsersMapper.class);
            Map<String, String> params = new HashMap<String, String>(2);
            params.put("username", username);
            params.put("password", password);
            mapper.setPassword(params);
        } catch (Exception e) {
            log.warn(e, "Exception when trying to set password to username: ", username);
        } finally {
            session.close();
        }
        log.warn("Set password for username: " + username);
    }

    public void updatePassword(String username, String password) {
        final SqlSession session = factory.openSession();
        try {
            log.debug("Updating password to user: ", username);
            final UsersMapper mapper = session.getMapper(UsersMapper.class);
            Map<String, String> params = new HashMap<String, String>(2);
            params.put("username", username);
            params.put("password", password);
            mapper.updatePassword(params);
        } catch (Exception e) {
            log.warn(e, "Exception when trying to update password to username: ", username);
        } finally {
            session.close();
        }
        log.warn("Updated password for username: " + username);
    }

    public void deletePassword(String username) {
        final SqlSession session = factory.openSession();
        try {
            log.debug("Deleting password for username: ", username);
            final UsersMapper mapper = session.getMapper(UsersMapper.class);
            mapper.deletePassword(username);
        } catch (Exception e) {
            log.warn(e, "Exception when trying to delete password for username: ", username);
        } finally {
            session.close();
        }
        log.warn("Deleted password for username: " + username);
    }

}
