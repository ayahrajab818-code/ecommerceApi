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

// Marks this class as a REST controller (returns JSON responses)
@RestController

// All endpoints in this controller start with /profile
@RequestMapping("/profile")

// Allows the front-end (different origin/port) to call these endpoints
@CrossOrigin

// FIXED: This ensures every endpoint in this controller requires a logged-in user.
// Without this, unauthenticated users might reach these endpoints depending on security config.
@PreAuthorize("isAuthenticated()")
public class ProfileController
{
    // DAO used to read/update profile data in the database
    private ProfileDao profileDao;

    // DAO used to look up the logged-in user record (to get their userId)
    private UserDao userDao;

    // Constructor injection: Spring provides the DAO implementations
    @Autowired
    public ProfileController(ProfileDao profileDao, UserDao userDao)
    {
        this.profileDao = profileDao;
        this.userDao = userDao;
    }

    // GET /profile
    // Returns the profile for the currently logged-in user
    @GetMapping("")
    public Profile getProfile(Principal principal)
    {
        try
        {
            // Principal contains the authenticated username from the JWT token
            String userName = principal.getName();

            // Find the user in the database so we can get their userId
            User user = userDao.getByUserName(userName);

            // FIXED: If user record doesn't exist, return 401 Unauthorized
            if (user == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);

            // Extract userId from the User object
            int userId = user.getId();

            // Load the profile tied to this userId
            Profile profile = profileDao.getByUserId(userId);

            // FIXED: If no profile exists, return 404 Not Found
            if(profile == null)
                throw new ResponseStatusException(HttpStatus.NOT_FOUND);

            // Return the profile JSON
            return profile;
        }
        catch(ResponseStatusException ex)
        {
            // Preserve intended HTTP errors like 401 or 404
            throw ex;
        }
        catch(Exception e)
        {
            // FIXED: Unexpected errors return 500 Internal Server Error
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Oops... our bad.");
        }
    }

    // PUT /profile
    // Updates the profile for the currently logged-in user
    @PutMapping("")
    public Profile updateProfile(@RequestBody Profile profile, Principal principal)
    {
        try
        {
            // Principal contains the authenticated username
            String userName = principal.getName();

            // Look up the user in the database to get their userId
            User user = userDao.getByUserName(userName);

            // FIXED: If user record doesn't exist, return 401 Unauthorized
            if (user == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);

            // Extract userId
            int userId = user.getId();

            // Update profile information for this userId
            profileDao.update(userId, profile);

            // Return the updated profile from the database (so the client gets the latest values)
            return profileDao.getByUserId(userId);
        }
        catch(ResponseStatusException ex)
        {
            // Preserve intended HTTP errors
            throw ex;
        }
        catch(Exception e)
        {
            // FIXED: Unexpected errors return 500 Internal Server Error
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Oops... our bad.");
        }
    }
}
