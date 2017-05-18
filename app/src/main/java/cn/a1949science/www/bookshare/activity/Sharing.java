package cn.a1949science.www.bookshare.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.view.animation.Transformation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.util.Date;
import java.util.List;

import cn.a1949science.www.bookshare.R;
import cn.a1949science.www.bookshare.bean.BookInfo;
import cn.a1949science.www.bookshare.bean.Book_Info;
import cn.a1949science.www.bookshare.bean.Shared_Info;
import cn.a1949science.www.bookshare.bean.SharingBook;
import cn.a1949science.www.bookshare.bean._User;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobDate;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.UpdateListener;

import static android.R.color.black;

public class Sharing extends AppCompatActivity implements View.OnClickListener{
    Context mContext = Sharing.this;
    ImageView image;
    View introduce_layout, expandView;
    TextView introduce,bookName,writename,bookPress,publishedDate,ISBN,time,bookOwner;
    ImageButton likeBtn,readBtn;
    String objectId,objectId1,introduce1,bookname1,writername1,OwnerName1,time1,phone,bookPress1,publishedDate1,ISBN1;
    int booknum1,textNum,userNum;
    boolean ifLike=false,ifRead=false,isExpand;
    Button borrowBtn,borrowBtn2,RefuseBtn;
    Toolbar toolbar;
    LinearLayout no_refuse, can_refuse;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book__info);

        initView();
        detail();
        onClick();
    }

    private void detail() {
        Bundle bundle = this.getIntent().getExtras();
        booknum1 = bundle.getInt("booknum");
        textNum = bundle.getInt("textNum");
        objectId = bundle.getString("objectId");
        userNum = bundle.getInt("userNum");
        if (textNum == 1) {
            borrowBtn.setText("等待书主响应");
            borrowBtn.setClickable(false);
            borrowBtn.setBackgroundColor(getResources().getColor(black));
        } else if (textNum == 2) {
            no_refuse.setVisibility(View.GONE);
            can_refuse.setVisibility(View.VISIBLE);
            //借书人信息查询
            BmobQuery<_User> query = new BmobQuery<>();
            query.addWhereEqualTo("userNum", userNum);
            query.findObjects(new FindListener<_User>() {
                @Override
                public void done(List<_User> list, BmobException e) {
                    if (e == null) {
                        AlertDialog dlg = new AlertDialog.Builder(mContext)
                                .setTitle("借书人信息")
                                .setMessage("借书人："+list.get(0).getUsername()+"\n"+"借书人电话："+list.get(0).getMobilePhoneNumber()+"\n"+
                                        "借书人地址："+list.get(0).getUserSchool())
                                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {

                                    }
                                })
                                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                    }
                                })
                                .create();
                        dlg.show();
                    } else {
                        Toast.makeText(mContext, "借书人信息查询失败：" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
            RefuseBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    refuseShare();
                }
            });
            borrowBtn2.setText("可以借出");
            borrowBtn2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog dlg = new AlertDialog.Builder(mContext)
                            .setTitle("确认可以借出？")
                            .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                }
                            })
                            .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    final ProgressDialog progress = new ProgressDialog(mContext);
                                    progress.setMessage("操作中...");
                                    progress.setCanceledOnTouchOutside(false);
                                    progress.show();
                                    //完善借书过程
                                    Shared_Info sharedInfo = new Shared_Info();
                                    sharedInfo.setIfAgree(true);
                                    sharedInfo.update(objectId, new UpdateListener() {
                                        @Override
                                        public void done(BmobException e) {
                                            if (e == null) {
                                                progress.dismiss();
                                                borrowBtn2.setText("等待借书电话");
                                                borrowBtn2.setClickable(false);
                                                RefuseBtn.setClickable(false);
                                                borrowBtn2.setBackgroundColor(getResources().getColor(black));
                                                RefuseBtn.setBackgroundColor(getResources().getColor(black));
                                            } else {
                                                Toast.makeText(mContext, "可以借出失败：" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                }
                            })
                            .create();
                    dlg.show();
                }
            });
        } else if (textNum == 3) {
            borrowBtn.setText("联系书主");
            borrowBtn.setClickable(true);
            borrowBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //查找owner
                    BmobQuery<_User> query = new BmobQuery<>();
                    query.addWhereEqualTo("username", OwnerName1);
                    //列表中不显示自己分享的书
                    query.findObjects(new FindListener<_User>() {
                        @Override
                        public void done(final List<_User> list, BmobException e) {
                            if (e == null) {
                                phone = list.get(0).getMobilePhoneNumber();
                                Intent it = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phone));
                                startActivity(it);
                                //Toast.makeText(mContext, phone, Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(mContext, "查询失败。", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            });
        } else if (textNum == 4) {
            no_refuse.setVisibility(View.GONE);
            can_refuse.setVisibility(View.VISIBLE);
            borrowBtn2.setText("书已借出");
            RefuseBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    refuseShare();
                }
            });
            borrowBtn2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog dlg = new AlertDialog.Builder(mContext)
                            .setTitle("确认已经借出？")
                            .setMessage("请确认借书人点击了“完成借入”按钮以保证图书的安全")
                            .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                }
                            })
                            .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    final ProgressDialog progress = new ProgressDialog(mContext);
                                    progress.setMessage("操作中...");
                                    progress.setCanceledOnTouchOutside(false);
                                    progress.show();
                                    //完成借书过程
                                    Shared_Info sharedInfo = new Shared_Info();
                                    sharedInfo.setIfLoan(true);
                                    sharedInfo.update(objectId, new UpdateListener() {
                                        @Override
                                        public void done(BmobException e) {
                                            if (e == null) {
                                                progress.dismiss();
                                                RefuseBtn.setClickable(false);
                                                borrowBtn2.setClickable(false);
                                                RefuseBtn.setBackgroundColor(getResources().getColor(black));
                                                borrowBtn2.setBackgroundColor(getResources().getColor(black));
                                                //Toast.makeText(mContext, "书已借出", Toast.LENGTH_SHORT).show();
                                            } else {
                                                Toast.makeText(mContext, "书已借出失败：" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                }
                            })
                            .create();
                    dlg.show();
                }
            });
        } else if (textNum == 5) {
            borrowBtn.setText("完成借入");
            borrowBtn.setClickable(true);
            borrowBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog dlg = new AlertDialog.Builder(mContext)
                            .setTitle("确认完成借入？")
                            .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                }
                            })
                            .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    final ProgressDialog progress = new ProgressDialog(mContext);
                                    progress.setMessage("操作中...");
                                    progress.setCanceledOnTouchOutside(false);
                                    progress.show();
                                    //完成借书过程
                                    Shared_Info sharedInfo = new Shared_Info();
                                    sharedInfo.setIfFinish(true);
                                    Date date = new Date(new Date().getTime() +Integer.parseInt(time1) * 24 * 60 * 60 * 1000);
                                    BmobDate now = new BmobDate(date);
                                    BmobDate now1 = BmobDate.createBmobDate("yyyy-MM-dd HH:mm:ss", now.getDate());
                                    sharedInfo.setFinishAt(now1);
                                    sharedInfo.update(objectId, new UpdateListener() {
                                        @Override
                                        public void done(BmobException e) {
                                            if (e == null) {
                                                progress.dismiss();
                                                borrowBtn.setClickable(false);
                                                borrowBtn.setBackgroundColor(getResources().getColor(black));
                                                //Toast.makeText(mContext, "完成借入", Toast.LENGTH_SHORT).show();
                                            } else {
                                                Toast.makeText(mContext, "完成借入失败：" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                }
                            })
                            .create();
                    dlg.show();
                }
            });
        } else if (textNum == 6) {
            borrowBtn.setText("确认归还");
            borrowBtn.setClickable(true);
            borrowBtn.setOnClickListener(new View.OnClickListener() {
                                             @Override
                                             public void onClick(View view) {
                                                 AlertDialog dlg = new AlertDialog.Builder(mContext)
                                                         .setTitle("确认归还？")
                                                         .setMessage("请确认书已归还")
                                                         .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                                             @Override
                                                             public void onClick(DialogInterface dialogInterface, int i) {

                                                             }
                                                         })
                                                         .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                                                             @Override
                                                             public void onClick(DialogInterface dialogInterface, int i) {
                                                                 final ProgressDialog progress = new ProgressDialog(mContext);
                                                                 progress.setMessage("操作中...");
                                                                 progress.setCanceledOnTouchOutside(false);
                                                                 progress.show();
                                                                 //完成借书过程
                                                                 Shared_Info sharedInfo = new Shared_Info();
                                                                 sharedInfo.setIfAffirm(true);
                                                                 sharedInfo.update(objectId, new UpdateListener() {
                                                                     @Override
                                                                     public void done(BmobException e) {
                                                                         if (e == null) {
                                                                             progress.dismiss();
                                                                             borrowBtn.setClickable(false);
                                                                             borrowBtn.setBackgroundColor(getResources().getColor(black));
                                                                             //Toast.makeText(mContext, "确认还书", Toast.LENGTH_SHORT).show();
                                                                         } else {
                                                                             Toast.makeText(mContext, "确认还书失败：" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                                         }
                                                                     }
                                                                 });
                                                             }
                                                         })
                                                         .create();
                                                 dlg.show();
                                             }
                                         });
        } else if (textNum == 7) {
            borrowBtn.setText("完成归还");
            borrowBtn.setClickable(true);
            borrowBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog dlg = new AlertDialog.Builder(mContext)
                            .setTitle("完成归还？")
                            .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                }
                            })
                            .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    final ProgressDialog progress = new ProgressDialog(mContext);
                                    progress.setMessage("操作中...");
                                    progress.setCanceledOnTouchOutside(false);
                                    progress.show();
                                    //完成借书过程
                                    Shared_Info sharedInfo = new Shared_Info();
                                    sharedInfo.setIfReturn(true);
                                    sharedInfo.update(objectId, new UpdateListener() {
                                        @Override
                                        public void done(BmobException e) {
                                            if (e == null) {
                                                //更新此书的状态
                                                Book_Info newBook = new Book_Info();
                                                newBook.setBeShared(false);
                                                newBook.update(objectId1, new UpdateListener() {
                                                    @Override
                                                    public void done(BmobException e) {
                                                        if (e == null) {

                                                            //更新用户借书状态
                                                            _User newUser = new _User();
                                                            newUser.setNeedReturn(false);
                                                            BmobUser bmobUser = BmobUser.getCurrentUser();
                                                            newUser.update(bmobUser.getObjectId(), new UpdateListener() {
                                                                @Override
                                                                public void done(BmobException e) {
                                                                    if (e == null) {
                                                                        borrowBtn.setClickable(false);
                                                                        borrowBtn.setBackgroundColor(getResources().getColor(black));
                                                                        ifNeedReturn();
                                                                    } else {
                                                                        Toast.makeText(mContext, "还书失败:" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                                    }
                                                                }
                                                            });
                                                            progress.dismiss();

                                                            //Toast.makeText(mContext, "更新信息成功", Toast.LENGTH_SHORT).show();
                                                        } else {
                                                            Toast.makeText(mContext, "更新信息失败:" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });

                                            } else {
                                                Toast.makeText(mContext, "完成归还失败：" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                }
                            })
                            .create();
                    dlg.show();
                }
            });
        }
        textNum = 0;

        final BmobQuery<BookInfo> query = new BmobQuery<>();
        query.addWhereEqualTo("bookNum",booknum1);
        query.findObjects(new FindListener<BookInfo>() {
            @Override
            public void done(List<BookInfo> list, BmobException e) {
                if (e == null) {
                    introduce1 = list.get(0).getIntroduction();
                    bookname1 = list.get(0).getBookName();
                    writername1 = list.get(0).getBookWriter();
                    bookPress1 = list.get(0).getBookPress();
                    publishedDate1 = list.get(0).getPublishedDate();
                    ISBN1 = list.get(0).getISBN();
                    introduce.setText(introduce1);
                    //descriptionView设置默认显示高度
                    introduce.setHeight(introduce.getLineHeight() * 3);
                    //根据高度来判断是否需要再点击展开
                    isExpand = introduce.getLineCount() <= 3;
                    introduce_layout.post(new Runnable() {
                        @Override
                        public void run() {
                            expandView.setVisibility(introduce.getLineCount() > 3 ? View.VISIBLE : View.GONE);
                        }
                    });
                    bookName.setText(bookname1);
                    writename.setText(writername1);
                    bookPress.setText(bookPress1);
                    publishedDate.setText(publishedDate1);
                    ISBN.setText(ISBN1);
                    Glide.with(mContext)
                            .load(list.get(0).getBookImage().getFileUrl())
                            .placeholder(R.drawable.wait)
                            .into(image);
                } else {
                    Toast.makeText(mContext, "查询失败。"+ e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    //看自己是否可以借书
    private void ifNeedReturn() {
        final _User bmobUser = BmobUser.getCurrentUser(_User.class);

        //查询是否有借书人为自己的借书信息
        BmobQuery<Shared_Info> query1 = new BmobQuery<>();
        query1.addWhereEqualTo("UserNum", bmobUser.getUserNum());
        query1.addWhereEqualTo("ifReturn", false);
        query1.addWhereEqualTo("ifRefuse", false);
        query1.findObjects(new FindListener<Shared_Info>() {
            @Override
            public void done(final List<Shared_Info> list, BmobException e) {
                if (e == null && list.size()==0) {
                    //更新用户借书状态
                    _User newUser = new _User();
                    newUser.setNeedReturn(false);
                    BmobUser bmobUser = BmobUser.getCurrentUser();
                    newUser.update(bmobUser.getObjectId(), new UpdateListener() {
                        @Override
                        public void done(BmobException e1) {
                            if (e1 == null) {
                                //Toast.makeText(mContext, "修改状态成功", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(mContext, "修改状态失败:" + e1.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } else if (e == null && list.size()!=0){
                    //更新用户借书状态
                    _User newUser = new _User();
                    newUser.setNeedReturn(true);
                    BmobUser bmobUser = BmobUser.getCurrentUser();
                    newUser.update(bmobUser.getObjectId(), new UpdateListener() {
                        @Override
                        public void done(BmobException e1) {
                            if (e1 == null) {
                                //Toast.makeText(mContext, "修改状态成功", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(mContext, "修改状态失败:" + e1.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });
    }

    //查找地址
    private void initView() {
        no_refuse = (LinearLayout) findViewById(R.id.no_refuse);
        can_refuse = (LinearLayout) findViewById(R.id.can_refuse);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.mipmap.left);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                overridePendingTransition(R.anim.slide_left_in,R.anim.slide_right_out);
            }
        });
        introduce_layout = findViewById(R.id.introduce_layout);
        introduce_layout.setOnClickListener(this);
        expandView = findViewById(R.id.expand_view);
        introduce = (TextView) findViewById(R.id.introduce);
        bookName = (TextView) findViewById(R.id.bookName);
        writename = (TextView) findViewById(R.id.writername);
        writename.setOnClickListener(this);
        bookPress = (TextView) findViewById(R.id.publishedDate);
        publishedDate = (TextView) findViewById(R.id.bookPress);
        ISBN = (TextView) findViewById(R.id.ISBN);
        time = (TextView) findViewById(R.id.time);
        bookOwner = (TextView) findViewById(R.id.bookOwner);
        likeBtn = (ImageButton)findViewById(R.id.likeBtn);
        likeBtn.setOnClickListener(this);
        readBtn = (ImageButton) findViewById(R.id.readBtn);
        readBtn.setOnClickListener(this);
        borrowBtn = (Button) findViewById(R.id.borrowBtn);
        borrowBtn.setOnClickListener(this);
        borrowBtn2 = (Button) findViewById(R.id.borrowBtn2);
        RefuseBtn = (Button) findViewById(R.id.RefuseBtn);
    }

    //点击事件
    private void onClick() {

        likeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!ifLike) {
                    likeBtn.setImageResource(R.mipmap.my_favourite);
                    ifLike = true;
                } else {
                    likeBtn.setImageResource(R.mipmap.favourite);
                    ifLike = false;
                }
            }
        });

        readBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!ifRead) {
                    readBtn.setImageResource(R.mipmap.seen);
                    ifRead = true;
                } else {
                    readBtn.setImageResource(R.mipmap.not_seen);
                    ifRead = false;
                }
            }
        });

    }

    //拒绝借出图书
    private void refuseShare() {
        AlertDialog dlg = new AlertDialog.Builder(mContext)
                .setTitle("确认拒绝借出？")
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        final ProgressDialog progress = new ProgressDialog(mContext);
                        progress.setMessage("操作中...");
                        progress.setCanceledOnTouchOutside(false);
                        progress.show();
                        //完善借书过程
                        Shared_Info sharedInfo = new Shared_Info();
                        sharedInfo.setIfRefuse(true);
                        sharedInfo.update(objectId, new UpdateListener() {
                            @Override
                            public void done(BmobException e) {
                                if (e == null) {
                                    progress.dismiss();
                                    borrowBtn.setClickable(false);
                                    borrowBtn.setBackgroundColor(getResources().getColor(black));
                                } else {
                                    Toast.makeText(mContext, "可以借出失败：" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                        //更新此书的状态
                        Book_Info newBook = new Book_Info();
                        newBook.setBeShared(false);
                        newBook.update(objectId1, new UpdateListener() {
                            @Override
                            public void done(BmobException e) {
                                if (e == null) {
                                    RefuseBtn.setClickable(false);
                                    borrowBtn2.setClickable(false);
                                    RefuseBtn.setBackgroundColor(getResources().getColor(black));
                                    borrowBtn2.setBackgroundColor(getResources().getColor(black));
                                } else {
                                    Toast.makeText(mContext, "更新信息失败:" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                })
                .create();
        dlg.show();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.introduce_layout:
                expand();
                break;
        }
    }

    //图书介绍的展开
    private void expand() {
        isExpand = !isExpand;
        introduce.clearAnimation();//清楚动画效果
        final int deltaValue;//默认高度，即前边由maxLine确定的高度
        final int startValue = introduce.getHeight();//起始高度
        int durationMillis = 350;//动画持续时间
        if (isExpand) {
            deltaValue = introduce.getLineHeight() * introduce.getLineCount() - startValue;
            RotateAnimation animation = new RotateAnimation(0, 180, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            animation.setDuration(durationMillis);
            animation.setFillAfter(true);
            expandView.startAnimation(animation);
        } else {
            deltaValue = introduce.getLineHeight() * 3 - startValue;
            RotateAnimation animation = new RotateAnimation(180, 0, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            animation.setDuration(durationMillis);
            animation.setFillAfter(true);
            expandView.startAnimation(animation);
        }
        Animation animation = new Animation() {
            protected void applyTransformation(float interpolatedTime, Transformation t) { //根据ImageView旋转动画的百分比来显示textview高度，达到动画效果
                introduce.setHeight((int) (startValue + deltaValue * interpolatedTime));
            }
        };
        animation.setDuration(durationMillis);
        introduce.startAnimation(animation);
    }
}
