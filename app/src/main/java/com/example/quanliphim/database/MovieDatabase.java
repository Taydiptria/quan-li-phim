package com.example.quanliphim.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.quanliphim.dao.MovieDao;
import com.example.quanliphim.entity.MovieEntity;

import java.util.concurrent.Executors;

@Database(
        entities = {MovieEntity.class},
        version = 2,
        exportSchema = false
)
public abstract class MovieDatabase extends RoomDatabase {

    private static volatile MovieDatabase INSTANCE;

    public abstract MovieDao movieDao();

    public static MovieDatabase getDatabase(Context context) {

        if (INSTANCE == null) {
            synchronized (MovieDatabase.class) {

                if (INSTANCE == null) {
                    final Context appContext = context.getApplicationContext();
                    INSTANCE = Room.databaseBuilder(
                            appContext,
                            MovieDatabase.class,
                            "movie_database"
                    )
                    .fallbackToDestructiveMigration()
                    .addCallback(new RoomDatabase.Callback() {
                        @Override
                        public void onCreate(@NonNull SupportSQLiteDatabase db) {
                            super.onCreate(db);
                            Executors.newSingleThreadExecutor().execute(() -> {
                                seedInitialMovies(appContext, INSTANCE.movieDao());
                            });
                        }
                    })
                    .build();

                    Executors.newSingleThreadExecutor().execute(() -> {
                        try {
                            MovieDao dao = INSTANCE.movieDao();
                            if (dao != null) {
                                dao.deleteDuplicateMoviesSync();
                                if (dao.getMovieCountSync() == 0) {
                                    seedInitialMovies(appContext, dao);
                                }
                            }
                        } catch (Exception ignored) {}
                    });
                }
            }
        }

        return INSTANCE;
    }

    private static void seedInitialMovies(Context context, MovieDao dao) {
        if (dao == null || context == null) return;
        if (dao.getMovieCountSync() > 0) return;

        String pkg = context.getPackageName();
        String poster1 = "android.resource://" + pkg + "/drawable/img";
        String poster2 = "android.resource://" + pkg + "/drawable/img_1";
        String poster3 = "android.resource://" + pkg + "/drawable/img_2";
        String poster4 = "android.resource://" + pkg + "/drawable/img_3";

        MovieEntity m1 = new MovieEntity();
        m1.title = "Avatar: Dòng Chảy Của Nước";
        m1.genre = "Viễn tưởng, Hành động";
        m1.director = "James Cameron";
        m1.year = 2022;
        m1.rating = 8.8f;
        m1.poster = poster1;
        m1.description = "Jake Sully sống cùng gia đình mới trên hành tinh Pandora. Khi một mối đe dọa quen thuộc trở lại, Jake phải chiến đấu cùng Neytiri và quân đội chủng tộc Na'vi để bảo vệ quê hương.";
        m1.review = "Hình ảnh kỹ xảo mãn nhãn đỉnh cao, cốt truyện giàu cảm xúc gia đình.";
        m1.favorite = true;
        m1.watched = true;

        MovieEntity m2 = new MovieEntity();
        m2.title = "Interstellar (Hố Đen Tử Thần)";
        m2.genre = "Viễn tưởng, Phiêu lưu";
        m2.director = "Christopher Nolan";
        m2.year = 2014;
        m2.rating = 9.2f;
        m2.poster = poster2;
        m2.description = "Trái Đất bị đe dọa bởi biến đổi khí hậu và nạn đói. Một nhóm nhà phi hành gia du hành qua hố đen vũ trụ để tìm kiếm hành tinh sống mới cho nhân loại.";
        m2.review = "Tuyệt tác điện ảnh viễn tưởng đỉnh cao của Christopher Nolan, âm nhạc Hans Zimmer xuất sắc.";
        m2.favorite = true;
        m2.watched = true;

        MovieEntity m3 = new MovieEntity();
        m3.title = "Avengers: Hồi Kết";
        m3.genre = "Hành động, Viễn tưởng";
        m3.director = "Anthony & Joe Russo";
        m3.year = 2019;
        m3.rating = 9.0f;
        m3.poster = poster3;
        m3.description = "Sau cú búng tay tàn phá của Thanos, các siêu anh hùng Avengers tập hợp lần cuối, du hành thời gian để khôi phục lại sự sống cho vũ trụ.";
        m3.review = "Cột mốc lịch sử của vũ trụ điện ảnh Marvel. Trận chiến cuối cùng vô cùng tráng lệ.";
        m3.favorite = false;
        m3.watched = true;

        MovieEntity m4 = new MovieEntity();
        m4.title = "Spider-Man: Không Còn Nhà";
        m4.genre = "Hành động, Viễn tưởng";
        m4.director = "Jon Watts";
        m4.year = 2021;
        m4.rating = 8.6f;
        m4.poster = poster4;
        m4.description = "Danh tính Peter Parker bị lộ. Cậu nhờ Doctor Strange làm phép xóa ký nhớ mọi người nhưng sự cố đa vũ trụ xảy ra, đưa các kẻ thù từ vũ trụ khác tới.";
        m4.review = "Màn kết hợp bùng nổ cảm xúc của cả 3 thế hệ Nhện!";
        m4.favorite = true;
        m4.watched = false;

        dao.insertMovie(m1);
        dao.insertMovie(m2);
        dao.insertMovie(m3);
        dao.insertMovie(m4);
    }
}
