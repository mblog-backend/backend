package st.coo.memo.common;

public enum QiniuRegion {

    huadong("华东-浙江"),
    huadongZheJiang2("华东-浙江2"),
    huabei("华北-河北"),
    huanan("华南-广东"),
    beimei("北美-洛杉矶"),
    xinjiapo("亚太-新加坡（原东南亚）");


    QiniuRegion(String desc) {
        this.desc = desc;
    }

    private String desc;

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
