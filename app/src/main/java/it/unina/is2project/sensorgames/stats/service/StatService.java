package it.unina.is2project.sensorgames.stats.service;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import it.unina.is2project.sensorgames.stats.database.dao.PlayerDAO;
import it.unina.is2project.sensorgames.stats.database.dao.StatOnePlayerDAO;
import it.unina.is2project.sensorgames.stats.database.dao.StatTwoPlayerDAO;
import it.unina.is2project.sensorgames.stats.entity.Player;
import it.unina.is2project.sensorgames.stats.entity.StatOnePlayer;
import it.unina.is2project.sensorgames.stats.StatOnePlayerRow;

public class StatService {
    private StatOnePlayerRow statOnePlayerRow;

    private PlayerDAO playerDAO;
    private StatOnePlayerDAO statOnePlayerDAO;
    private StatTwoPlayerDAO statTwoPlayerDAO;

    public StatService(Context context) {
        playerDAO = new PlayerDAO(context);
        statOnePlayerDAO = new StatOnePlayerDAO(context);
        statTwoPlayerDAO = new StatTwoPlayerDAO(context);
    }

    public List<StatOnePlayerRow> getStatOnePlayerList() {
        List<StatOnePlayerRow> ret = new ArrayList<StatOnePlayerRow>();
        List<StatOnePlayer> lista = statOnePlayerDAO.findAll();
        for (StatOnePlayer s : lista) {
            statOnePlayerRow = new StatOnePlayerRow();
            Player p = playerDAO.findById(s.getIdPlayer());
            statOnePlayerRow.setPlayer(p);
            statOnePlayerRow.setScore(s.getScore());
            statOnePlayerRow.setData(s.getData());
            ret.add(statOnePlayerRow);
        }
        return ret;
    }
}