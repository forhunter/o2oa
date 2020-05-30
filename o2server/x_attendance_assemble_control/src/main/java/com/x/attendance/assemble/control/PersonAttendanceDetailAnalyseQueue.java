package com.x.attendance.assemble.control;

import com.x.attendance.assemble.control.service.AttendanceDetailAnalyseServiceAdv;
import com.x.attendance.assemble.control.service.AttendanceDetailServiceAdv;
import com.x.attendance.assemble.control.service.AttendanceStatisticalCycleServiceAdv;
import com.x.attendance.assemble.control.service.AttendanceWorkDayConfigServiceAdv;
import com.x.attendance.entity.*;
import com.x.base.core.project.cache.ApplicationCache;
import com.x.base.core.project.logger.Logger;
import com.x.base.core.project.logger.LoggerFactory;
import com.x.base.core.project.queue.AbstractQueue;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import java.util.*;

/**
 * 对单个员工的打卡信息进行分析的队列
 */
public class PersonAttendanceDetailAnalyseQueue extends AbstractQueue<String> {

    protected Ehcache cache = ApplicationCache.instance().getCache( AttendanceStatisticalCycle.class);

    private AttendanceWorkDayConfigServiceAdv attendanceWorkDayConfigServiceAdv = new AttendanceWorkDayConfigServiceAdv();
    private AttendanceStatisticalCycleServiceAdv statisticalCycleServiceAdv = new AttendanceStatisticalCycleServiceAdv();
    private AttendanceDetailAnalyseServiceAdv detailAnalyseServiceAdv = new AttendanceDetailAnalyseServiceAdv();
    private AttendanceDetailServiceAdv detailServiceAdv = new AttendanceDetailServiceAdv();
    private static final Logger logger = LoggerFactory.getLogger(PersonAttendanceDetailAnalyseQueue.class);

    @Override
    protected void execute( String detailId ) throws Exception {
        AttendanceDetail record = detailServiceAdv.get( detailId );
        if( record != null ){
            logger.debug("system try to analyse attendance detail for person, Id:" + record.getId() );


            String cacheKey = ApplicationCache.concreteCacheKey( "map#all" );
            Element element = cache.get(cacheKey);
            try {

                Map<String, Map<String, List<AttendanceStatisticalCycle>>> statisticalCycleMap = null;
                if ((null != element) && (null != element.getObjectValue())) {
                    statisticalCycleMap = (Map<String, Map<String, List<AttendanceStatisticalCycle>>>) element.getObjectValue();
                }else{
                    statisticalCycleMap = statisticalCycleServiceAdv.getCycleMapFormAllCycles( false );
                }

                List<AttendanceWorkDayConfig> workDayConfigList = attendanceWorkDayConfigServiceAdv.listAll();

                detailAnalyseServiceAdv.analyseAttendanceDetail( record, workDayConfigList, statisticalCycleMap, false );
                logger.debug( "attendance detail analyse completed.person:" + record.getEmpName() + ", date:" + record.getRecordDateString());

            } catch (Exception e) {
                e.printStackTrace();
            }

            logger.debug("["+record.getEmpName()+"]["+ record.getRecordDateString() +"] attendance detail record analyse task execute completed。" );
        }else{
            logger.warn("attandence detail not exists, id:" + detailId );
        }

    }


}
