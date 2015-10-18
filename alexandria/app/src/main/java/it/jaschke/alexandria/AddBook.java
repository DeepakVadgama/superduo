package it.jaschke.alexandria;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import it.jaschke.alexandria.data.AlexandriaContract;
import it.jaschke.alexandria.scanner.SimpleScannerActivity;
import it.jaschke.alexandria.services.BookService;

public class AddBook extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int SCAN_REQUEST_CODE = 1;
    private EditText eanView;
    private final int LOADER_ID = 1;
    private View rootView;
    public static final String EAN_CONTENT = "eanContent";

    public AddBook() {
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (eanView != null) {
            outState.putString(EAN_CONTENT, eanView.getText().toString());
        }
    }

    @Override
    public View onCreateView(final LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_add_book, container, false);
        ViewHolder holder = setTag(rootView);

        eanView = holder.ean;
        holder.ean.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //no need
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //no need
            }

            @Override
            public void afterTextChanged(Editable s) {
                String ean = s.toString();
                //catch isbn10 numbers
                if (ean.length() == 10 && !ean.startsWith("978")) {
                    ean = "978" + ean;
                }
                if (ean.length() < 13) {
                    return;
                }
                //Once we have an ISBN, start a book intent
                fetchBook(ean);
            }
        });

        holder.scan_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(getActivity(), SimpleScannerActivity.class), SCAN_REQUEST_CODE);
            }
        });

        holder.save_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                eanView.setText("");
                clearFields();
            }
        });

        holder.delete_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent bookIntent = new Intent(getActivity(), BookService.class);
                bookIntent.putExtra(BookService.EAN, eanView.getText().toString());
                bookIntent.setAction(BookService.DELETE_BOOK);
                getActivity().startService(bookIntent);
                clearFields();
                eanView.setText("");
            }
        });

        if (savedInstanceState != null
                && savedInstanceState.getString(EAN_CONTENT) != null
                && !savedInstanceState.getString(EAN_CONTENT).isEmpty()) {
            eanView.setText(savedInstanceState.getString(EAN_CONTENT));
        }

        return rootView;
    }

    private void fetchBook(String ean) {

        clearFields();
        View progessBar = getActivity().findViewById(R.id.progressBar);
        if (progessBar != null) {
            progessBar.setVisibility(View.VISIBLE);
        }

        if (Utility.isConnectedToInternet(getActivity())) {
            Intent bookIntent = new Intent(getActivity(), BookService.class);
            bookIntent.putExtra(BookService.EAN, ean);
            bookIntent.setAction(BookService.FETCH_BOOK);
            getActivity().startService(bookIntent);
            AddBook.this.restartLoader();
        } else {
            Toast.makeText(getActivity(),
                    "Not connected to internet. Please check your internet connection",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void restartLoader() {
        getLoaderManager().restartLoader(LOADER_ID, null, this);
    }

    @Override
    public android.support.v4.content.Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (eanView.getText().length() == 0) {
            return null;
        }
        String eanStr = eanView.getText().toString();
        return new CursorLoader(
                getActivity(),
                AlexandriaContract.BookEntry.buildFullBookUri(Long.parseLong(eanStr)),
                null,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(android.support.v4.content.Loader<Cursor> loader, Cursor data) {

        View viewById = getActivity().findViewById(R.id.progressBar);
        viewById.setVisibility(View.GONE);
        ViewHolder holder = (ViewHolder) rootView.getTag();

        if (!data.moveToFirst()) {
            holder.notFound.setVisibility(View.VISIBLE);
            return;
        }

        // Fixed: If book is found, allow the buttons to show by hiding keyboard
        hideKeyboard(eanView);

        holder.notFound.setVisibility(View.GONE);

        String bookTitle = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.TITLE));
        holder.bookTitle.setText(bookTitle);

        String bookSubTitle = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.SUBTITLE));
        holder.bookSubTitle.setText(bookSubTitle);

        String authors = data.getString(data.getColumnIndex(AlexandriaContract.AuthorEntry.AUTHOR));
        if (authors == null) {
            holder.authors.setLines(1);
            holder.authors.setText(getString(R.string.author_unknown));
        } else {
            String[] authorsArr = authors.split(",");
            holder.authors.setLines(authorsArr.length);
            holder.authors.setText(authors.replace(",", "\n"));
        }
        String imgUrl = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.IMAGE_URL));
        if (Patterns.WEB_URL.matcher(imgUrl).matches()) {
            // Fixed: Same image is used in 3 places, use Picasso library to take advantage of caching
            Picasso.with(getActivity()).load(imgUrl).into(holder.bookCover);
            holder.bookCover.setVisibility(View.VISIBLE);
        }

        String categories = data.getString(data.getColumnIndex(AlexandriaContract.CategoryEntry.CATEGORY));
        holder.categories.setText(categories);

        holder.save_button.setVisibility(View.VISIBLE);
        holder.delete_button.setVisibility(View.VISIBLE);
    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader<Cursor> loader) {

    }

    private void clearFields() {
        ViewHolder viewHolder = (ViewHolder) rootView.getTag();
        viewHolder.bookTitle.setText("");
        viewHolder.bookSubTitle.setText("");
        viewHolder.authors.setText("");
        viewHolder.categories.setText("");
        viewHolder.bookCover.setVisibility(View.INVISIBLE);
        viewHolder.save_button.setVisibility(View.INVISIBLE);
        viewHolder.delete_button.setVisibility(View.INVISIBLE);
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        activity.setTitle(R.string.scan);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SCAN_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                String ean_content = data.getStringExtra(EAN_CONTENT);
                if (eanView == null) {
                    eanView = (EditText) rootView.findViewById(R.id.ean);
                }
                eanView.setText(ean_content);
            } else {
                Toast.makeText(getActivity(), "Barcode not correct. Please try again.",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static class ViewHolder {

        public TextView bookTitle;
        public TextView bookSubTitle;
        public TextView authors;
        public TextView categories;
        public ImageView bookCover;
        public View save_button;
        public View scan_button;
        public View delete_button;
        public EditText ean;
        public TextView notFound;
    }

    private ViewHolder setTag(View rootView) {
        ViewHolder viewHolder = new ViewHolder();
        viewHolder.bookTitle = ((TextView) rootView.findViewById(R.id.bookTitle));
        viewHolder.notFound = ((TextView) rootView.findViewById(R.id.notFound));
        viewHolder.bookSubTitle = ((TextView) rootView.findViewById(R.id.bookSubTitle));
        viewHolder.authors = ((TextView) rootView.findViewById(R.id.authors));
        viewHolder.categories = ((TextView) rootView.findViewById(R.id.categories));
        viewHolder.bookCover = (ImageView) rootView.findViewById(R.id.bookCover);
        viewHolder.scan_button = rootView.findViewById(R.id.scan_button);
        viewHolder.save_button = rootView.findViewById(R.id.save_button);
        viewHolder.delete_button = rootView.findViewById(R.id.delete_button);
        viewHolder.ean = (EditText) rootView.findViewById(R.id.ean);
        rootView.setTag(viewHolder);
        return viewHolder;
    }
}
