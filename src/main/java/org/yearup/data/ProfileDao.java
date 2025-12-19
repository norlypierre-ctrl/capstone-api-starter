package org.yearup.data;

import org.yearup.models.Profile;

public interface ProfileDao
{
    Profile getByUserId(int userId);
    Profile create(Profile profile);
    Profile update(int userId, Profile profile);
}
