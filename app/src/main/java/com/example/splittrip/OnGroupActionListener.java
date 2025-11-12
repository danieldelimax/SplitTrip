package com.example.splittrip;

import com.example.splittrip.model.GrupoViagem;

//Interface para ser implementada pelo Adapter e tratar ações
public interface OnGroupActionListener {
    void onGroupClick(GrupoViagem grupo, int position);
    void onGroupEdit(GrupoViagem grupo, int position);
}
