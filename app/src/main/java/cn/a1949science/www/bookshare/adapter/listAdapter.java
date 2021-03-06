package cn.a1949science.www.bookshare.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.util.List;

import cn.a1949science.www.bookshare.R;
import cn.a1949science.www.bookshare.activity.Book_detail;
import cn.a1949science.www.bookshare.bean.BookInfo;
import cn.a1949science.www.bookshare.bean.Like_Book;
import cn.a1949science.www.bookshare.bean.Read_Book;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.UpdateListener;

/**
 * Created by 高子忠 on 2017/5/19.
 */

public class listAdapter extends RecyclerView.Adapter<myAdapterRecyclerView.ViewHolder>
        implements View.OnClickListener{
    private Context context;
    private List<BookInfo> list;
    private String[] objects;
    private int selectNum;

    @Override
    public void onClick(View view) {

    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        View bookView;
        ImageView image;
        TextView book;
        TextView writer;

        public ViewHolder(View itemView) {
            super(itemView);
            bookView = itemView;
            image = (ImageView) itemView.findViewById(R.id.image);
            book = (TextView) itemView.findViewById(R.id.book);
            writer = (TextView) itemView.findViewById(R.id.writer);
        }
    }

    public listAdapter(Context context,List<BookInfo> list,String[] objects,int selectNum) {
        this.context = context;
        this.list = list;
        this.objects = objects;
        this.selectNum = selectNum;
    }

    @Override
    public myAdapterRecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.booklist_item, parent, false);
        final myAdapterRecyclerView.ViewHolder holder = new myAdapterRecyclerView.ViewHolder(view);
        holder.bookView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final int position = holder.getAdapterPosition();
                AlertDialog dlg = new AlertDialog.Builder(context)
                            .setTitle("删除此条信息？")
                            .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                }
                            })
                            .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    if (selectNum == 1) {
                                        //删除此Like_Book
                                        Like_Book book = new Like_Book();
                                        book.setObjectId(objects[position]);
                                        book.delete(new UpdateListener() {
                                            @Override
                                            public void done(BmobException e) {
                                                if (e == null) {
                                                    Toast.makeText(context, "删除成功", Toast.LENGTH_SHORT).show();
                                                } else {
                                                    Toast.makeText(context, "删除失败", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                    } else if (selectNum == 2) {
                                        //删除此Read_Book
                                        Read_Book book = new Read_Book();
                                        book.setObjectId(objects[position]);
                                        book.delete(new UpdateListener() {
                                            @Override
                                            public void done(BmobException e) {
                                                if (e == null) {
                                                    Toast.makeText(context, "删除成功", Toast.LENGTH_SHORT).show();
                                                } else {
                                                    Toast.makeText(context, "删除失败", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                    }
                                }
                            })
                            .create();
                    dlg.show();
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(myAdapterRecyclerView.ViewHolder holder, int position) {
        BookInfo book_info = list.get(position);
        String bookName = book_info.getBookName();
        String bookWriter = book_info.getBookWriter();
        Glide.with(context)
                .load(book_info.getBookImage().getFileUrl())
                .thumbnail(0.5f)
                .override((int)(context.getResources().getDisplayMetrics().density*120+0.5f),(int)(context.getResources().getDisplayMetrics().density*120+0.5f))
                .into(holder.image);
        holder.book.setText(bookName);
        holder.writer.setText(bookWriter);
    }


    @Override
    public int getItemCount() {
        return list.size();
    }
}
