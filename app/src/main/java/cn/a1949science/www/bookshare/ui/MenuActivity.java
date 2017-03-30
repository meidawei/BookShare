package cn.a1949science.www.bookshare.ui;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.lzy.imagepicker.bean.ImageItem;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cn.a1949science.www.bookshare.R;
import cn.a1949science.www.bookshare.adapter.MyAdapter;
import cn.a1949science.www.bookshare.bean.Book_Info;
import cn.a1949science.www.bookshare.bean.Shared_Info;
import cn.a1949science.www.bookshare.bean._User;
import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.BmobUpdateListener;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.QueryListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UploadFileListener;
import cn.bmob.v3.update.BmobUpdateAgent;
import cn.bmob.v3.update.UpdateResponse;
import cn.bmob.v3.update.UpdateStatus;
import de.hdodenhof.circleimageview.CircleImageView;

import static android.graphics.Color.BLACK;
import static android.graphics.Color.BLUE;
import static cn.a1949science.www.bookshare.R.id.imageView;

/**
 * Created by 高子忠 on 2017/3/22.
 */

public class MenuActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener{

    Context mContext = MenuActivity.this;
    Button borrowBtn,loanBtn,shareBtn,returnBtn,receiveBtn;
    Animation animation = null;
    ImageButton shortCut;
    ImageView favicon;
    TextView nickname;
    ListView listview;
    boolean clicked  = false;
    private static final int IMAGE_PICKER = 0;
    // 拍照
    private static final int PHOTO_REQUEST_CAREMA = 1;
    // 从相册选取照片
    private static final int PHOTO_REQUEST_GALLERY = 2;
    // 剪切照片
    private static final int PHOTO_REQUEST_CUT = 3;
    String picturePath="";
    private long exitTime = 0;
    Integer borrowBookNum,loanBookNum,textNum1,textNum2,textNum3,userNum;
    String objectId,time;
    View mine,headerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Bmob.initialize(this, "13d736220ecc496d7dcb63c7cf918ba7");
        //只在wifi下更新
        BmobUpdateAgent.setUpdateOnlyWifi(true);
        BmobUpdateAgent.setUpdateListener(new BmobUpdateListener() {
            @Override
            public void onUpdateReturned(int i, UpdateResponse updateResponse) {
                if (i == UpdateStatus.Yes) {//版本有更新
                    Toast.makeText(mContext, "版本有更新", Toast.LENGTH_SHORT).show();
                }else if(i == UpdateStatus.No){
                    Toast.makeText(mContext, "版本无更新", Toast.LENGTH_SHORT).show();
                }else if(i== UpdateStatus.EmptyField){
                    Toast.makeText(mContext, "请检查你AppVersion表的必填项，1、target_size（文件大小）是否填写；2、path或者android_url两者必填其中一项。", Toast.LENGTH_SHORT).show();
                }else if(i==UpdateStatus.IGNORED){
                    Toast.makeText(mContext, "该版本已被忽略更新", Toast.LENGTH_SHORT).show();
                }else if(i==UpdateStatus.ErrorSizeFormat){
                    Toast.makeText(mContext, "请检查target_size填写的格式，请使用file.length()方法获取apk大小。", Toast.LENGTH_SHORT).show();
                }else if(i==UpdateStatus.TimeOut){
                    Toast.makeText(mContext, "查询出错或查询超时", Toast.LENGTH_SHORT).show();
                }
            }
        });
        BmobUpdateAgent.update(this);

        findView();
        onClick();
        displayList();
        display();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        headerLayout = navigationView.getHeaderView(0);
        headerLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent it = new Intent(mContext,User_Info.class);
                startActivity(it);
                overridePendingTransition(R.anim.slide_right_in,R.anim.slide_left_out);
            }
        });
        nickname = (TextView) headerLayout.findViewById(R.id.nickname);
        favicon = (ImageView) headerLayout.findViewById(R.id.favicon);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }//点击两次返回键退出程序
        else if(System.currentTimeMillis() - exitTime > 2000) {
            Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
            exitTime = System.currentTimeMillis();
        } else {
            finish();
            System.exit(0);
            android.os.Process.killProcess(android.os.Process.myPid());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {

        }
        return super.onOptionsItemSelected(item);
    }
    //侧滑菜单中的点击事件
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.share) {
            Intent it = new Intent(mContext,My_Book_List.class);
            startActivity(it);
            overridePendingTransition(R.anim.slide_right_in,R.anim.slide_left_out);
        } else if (id == R.id.read) {
            Intent it = new Intent(mContext,MyRead.class);
            startActivity(it);
            overridePendingTransition(R.anim.slide_right_in,R.anim.slide_left_out);
        } else if (id == R.id.like) {
            Intent it = new Intent(mContext,MyLike.class);
            startActivity(it);
            overridePendingTransition(R.anim.slide_right_in,R.anim.slide_left_out);
        } else if (id == R.id.advice) {
            Intent it = new Intent(mContext,Advice_Page.class);
            startActivity(it);
            overridePendingTransition(R.anim.slide_right_in,R.anim.slide_left_out);
        } else if (id == R.id.refrush) {
            BmobUpdateAgent.forceUpdate(mContext);
        } else if (id == R.id.quit) {
            AlertDialog dlg = new AlertDialog.Builder(mContext)
                    .setTitle("确认退出？")
                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    })
                    .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            BmobUser.logOut();
                            BmobUser currentUser = BmobUser.getCurrentUser();
                            Intent intent = new Intent(mContext, Login_Page.class);
                            //清空源来栈中的Activity，新建栈打开相应的Activity
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            overridePendingTransition(R.anim.slide_left_in,R.anim.slide_right_out);
                        }
                    })
                    .create();
            dlg.show();
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    //查找地址
    private void findView() {
        shortCut = (ImageButton) findViewById(R.id.shortcut);
        listview = (ListView) findViewById(R.id.booklist);
        borrowBtn = (Button) findViewById(R.id.borrowBtn);
        loanBtn = (Button) findViewById(R.id.loanBtn);
        shareBtn = (Button) findViewById(R.id.shareBtn);
        returnBtn = (Button) findViewById(R.id.returnBtn);
        receiveBtn = (Button) findViewById(R.id.receiveBtn);
        mine = findViewById(R.id.mine);
    }
    //选择图书图片
    private void selectBookPicture() {
        Intent intent1 = new Intent(Intent.ACTION_PICK);
        intent1.setType("image");
        startActivityForResult(intent1, PHOTO_REQUEST_GALLERY);
        /*Intent intent = new Intent(this, ImagePicker.class);
        startActivityForResult(intent,IMAGE_PICKER);*/
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PHOTO_REQUEST_GALLERY && data != null) {

            Uri selectedImage = data.getData();

            String[] filePathColumn = {MediaStore.Images.Media.DATA};
            Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
            assert cursor != null;
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            picturePath = cursor.getString(columnIndex);
            cursor.close();
        }
        /*if (resultCode == com.lzy.imagepicker.ImagePicker.RESULT_CODE_ITEMS) {
            if (data != null && requestCode == IMAGE_PICKER) {
                ArrayList<ImageItem> images = (ArrayList<ImageItem>)
                        data.getSerializableExtra(com.lzy.imagepicker.ImagePicker.EXTRA_RESULT_ITEMS);
                String path =images.get(0).path;
                if (path != null) {
                    File file1 = new File(path);
                    path = file1.getAbsolutePath();
                }
                //用此方法加入到circleView当中
                com.lzy.imagepicker.ImagePicker.getInstance().getImageLoader().displayImage
                        (this,path,favicon,favicon.getWidth(),favicon.getHeight());
            } else {
                Toast.makeText(this, "没有数据", Toast.LENGTH_SHORT).show();
            }
        }*/
    }
    //点击事件
    private void onClick() {
        //分享按钮
        shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fold();
                //弹出信息框
                LayoutInflater inflater = getLayoutInflater();
                final View layout = inflater.inflate(R.layout.sharing, (ViewGroup) findViewById(R.id.sharing_Dialog));

                final Button bookPicture = (Button) layout.findViewById(R.id.bookPicture);
                //为上传图片按钮设置点击事件
                bookPicture.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        selectBookPicture();
                    }
                });
                final EditText bookName = (EditText) layout.findViewById(R.id.bookName);
                final EditText describe = (EditText) layout.findViewById(R.id.describe);
                final EditText bookWriter = (EditText) layout.findViewById(R.id.bookWriter);
                final EditText shareTime = (EditText) layout.findViewById(R.id.shareTime);
                shareTime.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(mContext);
                        final String[] sharetime = {"1","2","3","4","5","6","7"};
                        builder.setSingleChoiceItems(sharetime, 0, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                shareTime.setText(sharetime[i]);
                                dialogInterface.dismiss();
                            }
                        });
                        builder.show();
                    }
                });
                new AlertDialog.Builder(mContext)
                        .setTitle("图书共享")
                        .setPositiveButton("确认共享", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (bookName.getText().toString().equals("")  || describe.getText().toString().equals("")
                                        || bookWriter.getText().toString() .equals("") || shareTime.getText().toString() .equals("")||picturePath.equals("")) {
                                    Toast.makeText(mContext, "信息不全！！！", Toast.LENGTH_SHORT).show();
                                } else {
                                    final BmobFile bmobFile = new BmobFile(new File(picturePath));
                                    new AlertDialog.Builder(mContext)
                                            .setMessage("上传此图片？")
                                            .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    //等待框
                                                    final ProgressDialog progressDialog = new ProgressDialog(mContext);
                                                    progressDialog.setMessage("正在上传...");
                                                    progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                                                    progressDialog.setCanceledOnTouchOutside(false);
                                                    progressDialog.show();
                                                    //上传图片
                                                    bmobFile.uploadblock(new UploadFileListener() {
                                                        @Override
                                                        public void done(BmobException e) {
                                                            if (e == null) {
                                                                picturePath = "";
                                                                progressDialog.dismiss();
                                                                //上传其他信息
                                                                _User bmobUser = BmobUser.getCurrentUser(_User.class);
                                                                String username = bmobUser.getUsername();
                                                                Book_Info book = new Book_Info();
                                                                book.setOwner(bmobUser);
                                                                book.setOwnerName(username);
                                                                book.setBookName(bookName.getText().toString());
                                                                book.setBookDescribe(describe.getText().toString());
                                                                book.setBookWriter(bookWriter.getText().toString());
                                                                book.setkeepTime(shareTime.getText().toString());
                                                                book.setBeShared(false);
                                                                book.setBookPicture(bmobFile);
                                                                book.save(new SaveListener<String>() {
                                                                    @Override
                                                                    public void done(String s, BmobException e) {
                                                                        if (e == null) {
                                                                            Toast.makeText(mContext, "图书共享成功", Toast.LENGTH_SHORT).show();
                                                                            displayList();
                                                                        } else {
                                                                            Toast.makeText(mContext, "图书共享失败：" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                                        }
                                                                    }
                                                                });
                                                                progressDialog.dismiss();
                                                            } else {
                                                                Toast.makeText(mContext, "上传失败" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                                progressDialog.dismiss();
                                                            }
                                                        }
                                                    });
                                                }
                                            })
                                            .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    Toast.makeText(mContext, "请重新填写分享信息", Toast.LENGTH_SHORT).show();
                                                }
                                            })
                                            .show();
                                }
                            }
                        })
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        })
                        .setView(layout)
                        .show();
            }
        });

        //归还按钮
        returnBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fold();
                //弹出信息框
                LayoutInflater inflater = getLayoutInflater();
                _User bmobUser = BmobUser.getCurrentUser(_User.class);
                if (bmobUser.getNeedReturn()){
                    View layout = inflater.inflate(R.layout.raturning_yes, (ViewGroup) findViewById(R.id.returning_Yes_Dialog));
                    TextView returnTime = (TextView) layout.findViewById(R.id.returnTime);
                    returnTime.setText("截止时间："+ time);
                    new AlertDialog.Builder(mContext)
                            .setPositiveButton("确认还书", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    _User bmobUser = BmobUser.getCurrentUser(_User.class);
                                    //查询是否有借书人为自己的借书信息
                                    BmobQuery<Shared_Info> query = new BmobQuery<>();
                                    query.addWhereEqualTo("UserNum", bmobUser.getUserNum());
                                    query.addWhereEqualTo("ifReturn", false);
                                    query.findObjects(new FindListener<Shared_Info>() {
                                        @Override
                                        public void done(final List<Shared_Info> list, BmobException e) {
                                            if (e == null && list.size() != 0) {
                                                borrowBookNum = list.get(0).getBookNum();
                                                if (!list.get(0).getIfAffirm() && !list.get(0).getIfReturn()) {
                                                    textNum1 = 3;
                                                } else if (list.get(0).getIfAffirm() && !list.get(0).getIfReturn()) {
                                                    textNum1 = 7;
                                                }
                                                objectId = list.get(0).getObjectId();

                                                Bundle data = new Bundle();
                                                //利用Intent传递数据
                                                data.putInt("booknum",borrowBookNum);
                                                data.putInt("textNum",textNum1);
                                                data.putString("objectId",objectId);
                                                Intent intent = new Intent(mContext, Sharing.class);
                                                intent.putExtras(data);
                                                startActivity(intent);
                                                //Toast.makeText(mContext, "查询成功：共" + list.get(1).getBookName() + "条数据。", Toast.LENGTH_SHORT).show();
                                            } else {
                                                //Toast.makeText(mContext, "查询失败。", Toast.LENGTH_SHORT).show();
                                                borrowBtn.setClickable(false);
                                            }
                                        }
                                    });
                                }
                            })
                            .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                }
                            })
                            .setView(layout)
                            .setTitle("图书归还")
                            .show();
                }else {
                    View layout = inflater.inflate(R.layout.returning_no, (ViewGroup) findViewById(R.id.raturning_No_Dialog));
                    new AlertDialog.Builder(mContext)
                            .setPositiveButton("我要借书", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                }
                            })
                            .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                }
                            })
                            .setView(layout)
                            .setTitle("图书归还")
                            .show();
                }
            }
        });

        loanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fold();
                Bundle data = new Bundle();
                //利用Intent传递数据
                data.putInt("booknum",loanBookNum);
                data.putInt("textNum",textNum2);
                data.putString("objectId",objectId);
                data.putInt("userNum",userNum);
                Intent intent = new Intent(mContext, Sharing.class);
                intent.putExtras(data);
                startActivity(intent);
            }
        });

        borrowBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fold();
                Bundle data = new Bundle();
                //利用Intent传递数据
                data.putInt("booknum",borrowBookNum);
                data.putInt("textNum",textNum1);
                data.putString("objectId",objectId);
                Intent intent = new Intent(mContext, Sharing.class);
                intent.putExtras(data);
                startActivity(intent);
                //Toast.makeText(mContext, 123123 , Toast.LENGTH_SHORT).show();
            }
        });

        receiveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fold();
                Bundle data = new Bundle();
                //利用Intent传递数据
                data.putInt("booknum",loanBookNum);
                data.putInt("textNum",textNum3);
                data.putString("objectId",objectId);
                Intent intent = new Intent(mContext, Sharing.class);
                intent.putExtras(data);
                startActivity(intent);
            }
        });


        assert shortCut != null;
        shortCut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!clicked ) {
                    unfold();
                } else {
                    fold();
                }
            }
        });


    }
    //折叠按钮
    private void fold() {
        shareBtn.setVisibility(!clicked ? View.VISIBLE : View.GONE);
        returnBtn.setVisibility(!clicked ? View.VISIBLE : View.GONE);
        loanBtn.setVisibility(!clicked ? View.VISIBLE : View.GONE);
        borrowBtn.setVisibility(!clicked ? View.VISIBLE : View.GONE);
        receiveBtn.setVisibility(!clicked ? View.VISIBLE : View.GONE);

        displayList();
        animation = AnimationUtils.loadAnimation(mContext, R.anim.rotate_return);
        animation.setFillAfter(true);
        shortCut.startAnimation(animation);
        clicked = !clicked;
    }
    //展开按钮
    private void unfold() {
        shareBtn.setVisibility(!clicked ? View.VISIBLE : View.GONE);
        returnBtn.setVisibility(!clicked ? View.VISIBLE : View.GONE);
        loanBtn.setVisibility(!clicked ? View.VISIBLE : View.GONE);
        borrowBtn.setVisibility(!clicked ? View.VISIBLE : View.GONE);
        receiveBtn.setVisibility(!clicked ? View.VISIBLE : View.GONE);

        loanBtn.setClickable(false);
        borrowBtn.setClickable(false);
        receiveBtn.setClickable(false);

        returnBtn.setTextColor(BLUE);
        shareBtn.setTextColor(BLUE);
        loanBtn.setTextColor(BLACK);
        borrowBtn.setTextColor(BLACK);
        receiveBtn.setTextColor(BLACK);

        queryShareInfo();

        animation = AnimationUtils.loadAnimation(mContext, R.anim.rotate_add);
        animation.setFillAfter(true);
        shortCut.startAnimation(animation);
        clicked = !clicked;
    }
    //查询ShareInfo
    private void queryShareInfo() {

        final ProgressDialog progress = new ProgressDialog(mContext);
        progress.setMessage("正在查询信息...");
        progress.setCanceledOnTouchOutside(false);
        progress.show();

        _User bmobUser = BmobUser.getCurrentUser(_User.class);
        //查询是否有书主为自己的借书信息
        BmobQuery<Shared_Info> query = new BmobQuery<>();
        query.addWhereEqualTo("ownerName", bmobUser.getUsername());
        query.addWhereEqualTo("ifAffirm", false);
        query.findObjects(new FindListener<Shared_Info>() {
            @Override
            public void done(final List<Shared_Info> list, BmobException e) {
                if (e == null && list.size()!=0) {
                    //选择书
                    final android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(mContext);
                    final String[] booklist=new String[list.size()];
                    for (int i=0;i<list.size();i++) {
                        booklist[i]="借出第" + (i + 1) + "本书";
                    }
                    builder.setSingleChoiceItems(booklist, 0, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            loanBookNum = list.get(i).getBookNum();
                            if (!list.get(i).getIfAgree() && !list.get(i).getIfLoan() && !list.get(i).getIfFinish() && !list.get(i).getIfAffirm() && !list.get(i).getIfReturn()) {
                                //书主处理借书请求
                                textNum2 = 2;
                                loanBtn.setClickable(true);
                                loanBtn.setTextColor(BLUE);
                            } else if (list.get(i).getIfAgree() && !list.get(i).getIfLoan() && !list.get(i).getIfFinish() && !list.get(i).getIfAffirm() && !list.get(i).getIfReturn()) {
                                //书主确认书已借出
                                textNum2 = 4;
                                loanBtn.setClickable(true);
                                loanBtn.setTextColor(BLUE);
                            } else if (list.get(i).getIfAgree() && list.get(i).getIfLoan() && list.get(i).getIfFinish() && !list.get(i).getIfAffirm() && !list.get(i).getIfReturn()) {
                                textNum3 = 6;
                                receiveBtn.setClickable(true);
                                receiveBtn.setTextColor(BLUE);
                            }
                            objectId = list.get(i).getObjectId();
                            userNum = list.get(i).getUserNum();
                            //Toast.makeText(mContext, "查询成功：共" + list.get(1).getBookName() + "条数据。", Toast.LENGTH_SHORT).show();
                            dialogInterface.dismiss();
                        }
                    });
                    builder.show();
                } else {
                    //Toast.makeText(mContext, "查询失败。", Toast.LENGTH_SHORT).show();
                    borrowBtn.setTextColor(BLACK);
                }
            }
        });

        //查询是否有借书人为自己的借书信息
        BmobQuery<Shared_Info> query1 = new BmobQuery<>();
        query1.addWhereEqualTo("UserNum", bmobUser.getUserNum());
        query1.addWhereEqualTo("ifReturn", false);
        query1.findObjects(new FindListener<Shared_Info>() {
            @Override
            public void done(final List<Shared_Info> list, BmobException e) {
                if (e == null && list.size()!=0) {
                    borrowBookNum = list.get(0).getBookNum();
                    if (!list.get(0).getIfAgree() && !list.get(0).getIfLoan() && !list.get(0).getIfFinish() && !list.get(0).getIfAffirm() && !list.get(0).getIfReturn()) {
                        //借书人刚发送借书请求
                        textNum1 = 1;
                        borrowBtn.setClickable(true);
                        borrowBtn.setTextColor(BLUE);
                    } else if (list.get(0).getIfAgree() && !list.get(0).getIfLoan() && !list.get(0).getIfFinish() && !list.get(0).getIfAffirm() && !list.get(0).getIfReturn()) {
                        //书主同意了借书人的请求
                        textNum1 = 3;
                        borrowBtn.setClickable(true);
                        borrowBtn.setTextColor(BLUE);
                    } else if (list.get(0).getIfAgree() && list.get(0).getIfLoan() && !list.get(0).getIfFinish() && !list.get(0).getIfAffirm() && !list.get(0).getIfReturn()) {
                        //书主已借出书
                        textNum1 = 5;
                        borrowBtn.setClickable(true);
                        borrowBtn.setTextColor(BLUE);
                    }else if (list.get(0).getIfAgree() && list.get(0).getIfLoan() && list.get(0).getIfFinish() && !list.get(0).getIfAffirm() && !list.get(0).getIfReturn()) {
                        //借书过程完成
                        time = list.get(0).getFinishAt().getDate();
                        borrowBtn.setTextColor(BLACK);
                    }
                    objectId = list.get(0).getObjectId();
                    progress.dismiss();
                    //Toast.makeText(mContext, "查询成功：共" + list.get(1).getBookName() + "条数据。", Toast.LENGTH_SHORT).show();
                } else {
                    progress.dismiss();
                    //Toast.makeText(mContext, "查询失败。", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    //显示头像、昵称
    private void display() {
        BmobUser bmobUser = BmobUser.getCurrentUser();
        bmobUser.getObjectId();
        BmobQuery<_User> query = new BmobQuery<_User>();
        query.getObject(bmobUser.getObjectId(), new QueryListener<_User>() {
            @Override
            public void done(_User user, BmobException e) {
                if (e == null) {
                    nickname.setText(user.getNickname());
                    Glide.with(mContext)
                            .load(user.getFavicon().getFileUrl())
                            .override((int)(mContext.getResources().getDisplayMetrics().density*60+0.5f),(int)(mContext.getResources().getDisplayMetrics().density*60+0.5f))
                            .centerCrop()
                            .placeholder(R.drawable.wait)
                            .error(R.drawable.wait)
                            .into(favicon);
                } else {
                    Toast.makeText(mContext, "昵称、头像显示失败:" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    //显示列表
    private void displayList() {
        //查找book
        BmobQuery<Book_Info> query1 = new BmobQuery<>();
        //查找出有ownerNum的信息
        query1.addWhereEqualTo("BeShared", false);
        //列表中不显示自己分享的书
        /*_User bmobUser = BmobUser.getCurrentUser(_User.class);
        String username = bmobUser.getUsername();
        query.addWhereNotEqualTo("ownerName", username);*/
        query1.setLimit(50);
        query1.findObjects(new FindListener<Book_Info>() {
            @Override
            public void done(final List<Book_Info> list, BmobException e) {
                if (e == null) {
                    //Toast.makeText(mContext, "查询成功：共" + list.size() + "条数据。", Toast.LENGTH_SHORT).show();
                    final int[] bookNum = new int[list.size()];
                    for (int i=0;i<list.size();i++) {
                        bookNum[i] = list.get(i).getBookNum();
                    }
                    listview.setAdapter(new MyAdapter(mContext,list));

                    listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                            if (clicked ) {
                                fold();
                            }
                            Bundle data = new Bundle();
                            //利用Intent传递数据
                            //data.putString("imageid", list.get(i).getBookPicture().getFileUrl());
                            data.putInt("booknum",bookNum[i]);
                            data.putString("objectId",list.get(i).getObjectId());
                            Intent intent = new Intent(mContext, Book_detail.class);
                            intent.putExtras(data);
                            startActivity(intent);
                            overridePendingTransition(R.anim.slide_right_in,R.anim.slide_left_out);
                        }
                    });
                    //Toast.makeText(mContext, "查询成功：共" + list.get(1).getBookName() + "条数据。", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(mContext, "查询失败。", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
}