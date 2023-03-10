<template>
  <div>
    <el-button type="danger" @click="batchDelete">批量删除</el-button>
    <el-button v-if="draggable" @click="batchSave">批量保存</el-button>
    <el-switch
      style="display: block"
      v-model="draggable"
      active-color="#13ce66"
      inactive-color="#ff4949"
      active-text="开启拖拽"
      inactive-text="关闭拖拽"
    >
    </el-switch>
    <el-tree
      :data="menus"
      :props="defaultProps"
      :default-expanded-keys="expandKeys"
      :expand-on-click-node="false"
      show-checkbox
      node-key="catId"
      ref="menuTree"
      :draggable="draggable"
      :allow-drop="allowDrop"
      @node-drop="handleDrop"
    >
      <span class="custom-tree-node" slot-scope="{ node, data }">
        <span>{{ node.label }}</span>
        <span>
          <el-button
            v-if="node.level <= 2"
            type="text"
            size="mini"
            @click="() => append(data)"
          >
            添加
          </el-button>
          <!-- <el-button v-if="node.childNodes == null || node.childNodes.length == 0" type="text" size="mini" @click="() => remove(node, data)">
          删除
        </el-button> -->
          <el-button
            v-if="node.childNodes == null || node.childNodes.length == 0"
            type="text"
            size="mini"
            @click="() => remove(node, data)"
          >
            删除
          </el-button>
          <el-button type="text" size="mini" @click="() => edit(data)">
            修改</el-button
          >
        </span>
      </span>
    </el-tree>

    <!-- 嵌套表单 -->
    <el-dialog :title="title" :visible.sync="dialogVisible" width="30%">
      <el-form :model="category">
        <el-form-item label="分类名称">
          <el-input v-model="category.name" autocomplete="off"></el-input>
        </el-form-item>

        <el-form-item label="icon">
          <el-input v-model="category.icon" autocomplete="off"></el-input>
        </el-form-item>

        <el-form-item label="计量单位">
          <el-input
            v-model="category.productUnit"
            autocomplete="off"
          ></el-input>
        </el-form-item>

        <el-form-item label="商品数量">
          <el-input
            v-model="category.productCount"
            autocomplete="off"
          ></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="submitData">确 定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
export default {
  data() {
    return {
      //最大层级
      maxLevel: 0,
      updateNodes: [],
      pCid: [],
      //是否开启拖拽
      draggable: false,
      dialogType: "", // 对话框类型
      title: "", // 对话框标题
      category: {
        name: "",
        parentCid: "",
        catLevel: "",
        showStatus: 1,
        sort: 0,
        icon: "",
        productUnit: "",
        productCount: ""
      },
      dialogVisible: false,
      expandKeys: [],
      menus: [],
      data: [],
      defaultProps: {
        children: "children",
        label: "name"
      }
    };
  },

  methods: {
    //批量修改保存
    batchSave() {
      this.$http({
        url: this.$http.adornUrl("/product/category/update/sort"),
        method: "post",
        data: this.$http.adornData(this.updateNodes, false)
      }).then(({ data }) => {
        this.$message({
          message: "分类顺序等修改成功",
          type: "success"
        });
        //刷新出新的菜单
        this.getMenus();
        //设置需要默认展开的菜单
        this.expandKeys = this.pCid;
        this.updateNodes = [];
        this.maxLevel = 0;
        // this.pCid = 0;
      });
    },

    //拖拽成功后的处理
    handleDrop(draggingNode, dropNode, dropType, ev) {
      console.log("handleDrop: ", draggingNode, dropNode, dropType);
      //1、当前节点最新的父节点id
      let pCid = 0;
      //兄弟节点
      let siblings = null;
      if (dropType == "before" || dropType == "after") {
        pCid =
          dropNode.parent.data.catId == undefined
            ? 0
            : dropNode.parent.data.catId;
        siblings = dropNode.parent.childNodes;
      } else {
        pCid = dropNode.data.catId;
        siblings = dropNode.childNodes;
      }
      this.pCid.push(pCid);

      //2、当前拖拽节点的最新顺序，
      for (let i = 0; i < siblings.length; i++) {
        if (siblings[i].data.catId == draggingNode.data.catId) {
          //如果遍历的是当前正在拖拽的节点
          let catLevel = draggingNode.level;
          if (siblings[i].level != draggingNode.level) {
            //当前节点的层级发生变化
            catLevel = siblings[i].level;
            //修改他子节点的层级
            this.updateChildNodeLevel(siblings[i]);
          }
          this.updateNodes.push({
            catId: siblings[i].data.catId,
            sort: i,
            parentCid: pCid,
            catLevel: catLevel
          });
        } else {
          this.updateNodes.push({ catId: siblings[i].data.catId, sort: i });
        }
      }

      //3、当前拖拽节点的最新层级
      console.log("updateNodes", this.updateNodes);
    },

    //更新子节点层级
    updateChildNodeLevel(node) {
      if (node.childNodes.length > 0) {
        for (let i = 0; i < node.childNodes.length; i++) {
          var cNode = node.childNodes[i].data;
          this.updateNodes.push({
            catId: cNode.catId,
            catLevel: node.childNodes[i].level
          });
          this.updateChildNodeLevel(node.childNodes[i]);
        }
      }
    },

    //拖拽时判定目标节点能否被放置。type 参数有三种情况：'prev'、'inner' 和 'next'，
    //分别表示放置在目标节点前、插入至目标节点和放置在目标节点后
    allowDrop(draggingNode, dropNode, type) {
      //1）、被拖动的当前节点总层数
      console.log("allowDrop:", draggingNode, dropNode, type);
      this.countNodeLevel(draggingNode);
      //当前正在拖动的节点+父节点所在的深度不大于3即可
      let deep = Math.abs(this.maxLevel - draggingNode.level) + 1;
      console.log("深度: ", deep);

      //   this.maxLevel
      if (type == "inner") {
        // console.log(
        //   `this.maxLevel：${this.maxLevel}；draggingNode.data.catLevel：${draggingNode.data.catLevel}；dropNode.level：${dropNode.level}`
        // );
        return deep + dropNode.level <= 3;
      } else {
        return deep + dropNode.parent.level <= 3;
      }
    },

    //层级
    countNodeLevel(node) {
      //找到所有子节点，求出最大深度
      if (node.childNodes != null && node.childNodes.length > 0) {
        for (let i = 0; i < node.childNodes.length; i++) {
          if (node.childNodes[i].level > this.maxLevel) {
            this.maxLevel = node.childNodes[i].level;
          }
          this.countNodeLevel(node.childNodes[i]);
        }
      }
    },

    //点击append和remove后触发的方法
    append(data) {
      this.title = "新增";
      this.dialogVisible = true;
      //获取添加新节点的属性
      this.category.parentCid = data.catId;
      this.category.showStatus = data.showStatus;
      this.category.catLevel = data.catLevel * 1 + 1;
      this.category.sort = data.sort;
      this.dialogType = "add";
    },

    edit(data) {
      //console.log("append", data);
      this.title = "修改";
      this.dialogVisible = true;
      this.dialogType = "edit";
      //发送请求获取当前节点最新的数据
      this.$http({
        url: this.$http.adornUrl(`/product/category/info/${data.catId}`),
        method: "get"
      }).then(({ data }) => {
        //请求成功 回显
        //console.log("要回显的数据", data);
        this.category.name = data.data.name;
        this.category.catId = data.data.catId;
        this.category.icon = data.data.icon;
        this.category.productUnit = data.data.productUnit;
        this.category.parentCid = data.data.parentCid;
        this.category.catLevel = data.data.catLevel;
        this.category.sort = data.data.sort;
        this.category.showStatus = data.data.showStatus;
      });
    },

    //数据提交
    submitData() {
      if (this.dialogType == "add") {
        this.addCategory();
      } else if (this.dialogType == "edit") {
        this.editCategory();
      }
    },

    editCategory() {
      var { catId, name, icon, productUnit } = this.category;
      this.$http({
        url: this.$http.adornUrl("/product/category/update"),
        method: "post",
        data: this.$http.adornData({ catId, name, icon, productUnit }, false)
      }).then(({ data }) => {
        this.$message({
          message: `${this.category.name}保存成功`,
          type: "success"
        });
        //关闭对话框
        this.dialogVisible = false;
        //刷新出新的菜单
        this.getMenus();
        //设置需要默认展开的菜单
        this.expandKeys = [this.category.parentCid];
      });
    },

    addCategory() {
      console.log("提交的三级分类数据", this.category);
      this.$http({
        url: this.$http.adornUrl("/product/category/save"),
        method: "post",
        data: this.$http.adornData(this.category, false)
      }).then(({ data }) => {
        this.$message({
          message: `${this.category.name}保存成功`,
          type: "success"
        });
        //关闭对话框
        this.dialogVisible = false;
        //刷新出新的菜单
        this.getMenus();
        //设置需要默认展开的菜单
        this.expandKeys = [this.category.parentCid];
      });
    },

    //批量删除
    batchDelete() {
      //要删除元素id
      let catIds = [];
      //要删除元素名称  为后面消息提示弹框
      let catNames = [];
      let checkedNodes = this.$refs.menuTree.getCheckedNodes();
      for (let i = 0; i < checkedNodes.length; i++) {
        catIds.push(checkedNodes[i].catId);
        catNames.push(checkedNodes[i].name);
      }
      //弹窗,是否删除
      this.$confirm(`是否批量删除[${catNames}]分类?`, "是否删除", {
        distinguishCancelAndClose: true,
        confirmButtonText: "确认",
        cancelButtonText: "取消"
      })
        .then(() => {
          this.$http({
            url: this.$http.adornUrl("/product/category/delete"),
            method: "post",
            data: this.$http.adornData(catIds, false)
          })
            .then(({ data }) => {
              this.$message({
                message: "分类删除成功",
                type: "success"
              });
              this.getMenus();
            })
            .catch(() => {});
        })
        .catch(() => {});
    },

    //删除节点信息
    remove(node, data) {
      console.log(node);
      this.$confirm(`是否删除【${data.name}】菜单?`, "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning"
      })
        .then(() => {
          var ids = [data.catId];
          this.$http({
            url: this.$http.adornUrl("/product/category/delete"),
            method: "post",
            data: this.$http.adornData(ids, false)
          }).then(({ data }) => {
            this.$message({
              message: "菜单删除成功",
              type: "success"
            });
            // 刷新出新的菜单
            this.getMenus();
            // 设置需要默认展开的菜单
            this.expandKeys = [node.parent.data.catId];
          });
        })
        .catch(() => {});
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
    }
  },
  created() {
    this.getMenus();
  }
};
</script>

<style></style>
