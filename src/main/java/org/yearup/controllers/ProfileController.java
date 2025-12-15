package org.yearup.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.yearup.data.ProfileDao;
import org.yearup.data.UserDao;
import org.yearup.models.Profile;
import org.yearup.models.User;

import java.security.Principal;

@RestController
@RequestMapping("/profile")
@CrossOrigin
@PreAuthorize("isAuthenticated()")
public class ProfileController
{
    private ProfileDao profileDao;
    private UserDao userDao;

    @Autowired
    public ProfileController(ProfileDao profileDao, UserDao userDao) //Constructor injection for ProfileDao and UserDao
    {
        this.profileDao = profileDao;
        this.userDao = userDao;
    }

    @GetMapping("")
    public Profile getProfile(Principal principal)
    {
        try
        {
            String userName = principal.getName();   // Get currently logged-in username
            User user = userDao.getByUserName(userName);   // Find user in database
            if (user == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);

            int userId = user.getId();
            Profile profile = profileDao.getByUserId(userId); // Retrieve user's profile

            if(profile == null)
                throw new ResponseStatusException(HttpStatus.NOT_FOUND);
            return profile;
        }
        catch(ResponseStatusException ex)
        { throw ex;
        }  catch(Exception e)
        {  throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Oops... our bad.");
        }
    }

    @PutMapping("")
    public Profile updateProfile(@RequestBody Profile profile, Principal principal)
    {
        try
        {
            String userName = principal.getName();            // Get currently logged-in username
            User user = userDao.getByUserName(userName);
            if (user == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);

            int userId = user.getId();
            profileDao.update(userId, profile);  // Update user's profile

            return profileDao.getByUserId(userId); // Return updated profile
        }
        catch(ResponseStatusException ex)
        {  throw ex;
        } catch(Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Oops... our bad.");
        }
    }
}