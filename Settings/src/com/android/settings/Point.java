package com.android.settings;
class Point {
		private static final int mPerCellSize = 23;
        private int m_x;
        private int m_y;
        
        public Point(int x, int y) {
            m_x = x;
            m_y = y;
        }

        public void setPoint(int x, int y) {
            this.m_x = x;
            this.m_y = y;
        }
        
        public int getXPoint(){
        	return m_x;
        }
        public int getYPoint(){
        	return m_y;
        }
        public float getXStep(){
        	return m_x/mPerCellSize;
        }
        public float getYStep(){
        	return m_y/mPerCellSize;
        }
    };