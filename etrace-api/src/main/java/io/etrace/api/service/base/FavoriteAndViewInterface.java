package io.etrace.api.service.base;

public interface FavoriteAndViewInterface {

    void updateUserFavorite(long id);

    void updateUserView(long id);

    void deleteUserFavorite(long id);
}
