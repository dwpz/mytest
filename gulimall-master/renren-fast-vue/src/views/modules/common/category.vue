<template>
    <el-tree :data="menus" :props="defaultProps" node-key="catId" ref="menuTree" @node-click="nodeclick">
    </el-tree>
</template>
<script>
export default {
    data() {
        return {
            menus: [],
            expandedKey: [],
            defaultProps: {
                children: "children",
                label: "name"
            }
        };
    },
    methods: {
        //子页面向父页面传递信息
        nodeclick(data, node, component) {
            //console.log("子组件categroy的节点被点击:", data, node, component);
            this.$emit("tree-node-click", data, node, component); //向父组件发送tree-node-click事件
        },
        //获取分类列表
        getMenus() {
            this.$http({
                url: this.$http.adornUrl("/product/category/list/tree"),
                method: "get"
            }).then(({ data }) => {
                //console.log(data);
                this.menus = data.data;
            });
        },
        handleNodeClick(data) {
            console.log(data);
        }
    },
    created() {
        this.getMenus();
    }
};
</script>
<style lang='scss' scoped></style>