package com.nawin.androidmvparchitecture.taggedquestion;


import android.content.Context;

import com.nawin.androidmvparchitecture.R;
import com.nawin.androidmvparchitecture.data.Data;
import com.nawin.androidmvparchitecture.data.model.TagItems;
import com.nawin.androidmvparchitecture.data.model.Tags;
import com.nawin.androidmvparchitecture.data.model.api.BaseResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.nawin.androidmvparchitecture.utils.Commons.cancel;
import static com.nawin.androidmvparchitecture.utils.Commons.isEmpty;
import static com.nawin.androidmvparchitecture.utils.Commons.isNetworkAvailable;

/**
 * Created by nawin on 6/14/17.
 */

class TaggedQuestionsPresenter implements TaggedQuestionsContract.Presenter {
    private final int LIMIT = 10;
    private TaggedQuestionsContract.View view;
    private Call<BaseResponse<Tags>> call;
    private int offset;

    TaggedQuestionsPresenter(TaggedQuestionsContract.View view) {
        this.view = view;
    }

    @Override
    public void start() {

        if (!isNetworkAvailable(view.getContext())) {
            view.showNetworkNotAvailableError();
            return;
        }
        view.showProgress();
        this.offset = 0;
        call = Data.getInstance(view.getContext()).requestTags(offset, LIMIT, new Callback<BaseResponse<Tags>>() {
            @Override
            public void onResponse(Call<BaseResponse<Tags>> call, Response<BaseResponse<Tags>> response) {
                if (response != null && response.isSuccessful()) {
                    int itemCount = response.body().getResponse().getItemCount();
                    List<TagItems> items = response.body().getResponse().getItems();
                    if (itemCount > 0 && !isEmpty(items)) {
                        final int count = items.size();
                        offset += count;
                        view.showTagsLoadSuccess(items, itemCount > offset);
                    } else {
                        view.showEmptyTags(R.string.data_not_available);
                    }
                } else {
                    view.showTagsLoadError(R.string.server_error);
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<Tags>> call, Throwable t) {
                view.showTagsLoadError(R.string.server_error);
            }
        });
    }

    @Override
    public void stop() {
        cancel(call);
    }

    @Override
    public void onLoadMore() {
        view.showLoadMoreProgress();
        call = Data.getInstance(view.getContext()).requestTags(offset, LIMIT, new Callback<BaseResponse<Tags>>() {

            @Override
            public void onResponse(Call<BaseResponse<Tags>> call, Response<BaseResponse<Tags>> response) {
                if (response != null && response.isSuccessful()) {
                    int itemCount = response.body().getResponse().getItemCount();
                    List<TagItems> items = response.body().getResponse().getItems();
                    if (itemCount > 0 && !isEmpty(items)) {
                        final int count = items.size();
                        offset += count;
                        view.showMoreTags(items, itemCount > offset);
                    } else {
                        view.onLoadComplete();
                    }
                } else {
                    view.showLoadMoreError();
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<Tags>> call, Throwable t) {
                view.showLoadMoreError();
            }
        });
    }

    @Override
    public void onTaggedQuestionSelected(TagItems items) {

    }

    @Override
    public void onLogout() {
        Data.getInstance(view.getContext()).logout();
    }
}
