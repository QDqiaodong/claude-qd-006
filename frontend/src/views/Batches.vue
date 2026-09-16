<template>
  <div>
    <el-card shadow="never">
      <template #header>
        <div style="display:flex;align-items:center;gap:12px">
          <span>菜品</span>
          <el-button size="small" type="success" @click="openDish">新增菜品</el-button>
          <span style="margin-left:auto;color:#909399">
            在售 {{ dishes.filter(d => d.status === '在售').length }} 个 / 共 {{ dishes.length }} 个
          </span>
        </div>
      </template>
      <el-table :data="dishes" border stripe size="small">
        <el-table-column prop="code" label="菜品编号" width="120" />
        <el-table-column prop="name" label="菜名" min-width="150" />
        <el-table-column prop="category" label="类别" width="100" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === '在售' ? 'success' : 'info'">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="110">
          <template #default="{ row }">
            <el-button link :type="row.status === '在售' ? 'danger' : 'primary'" @click="toggleDish(row)">
              {{ row.status === '在售' ? '停用' : '上架' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-card shadow="never" style="margin-top:16px">
      <template #header>
        <div style="display:flex;align-items:center;gap:12px;flex-wrap:wrap">
          <span>备餐批次</span>
          <el-select v-model="query.status" placeholder="按状态" clearable size="small" style="width:130px">
            <el-option v-for="s in statuses" :key="s" :label="s" :value="s" />
          </el-select>
          <el-date-picker v-model="query.date" type="date" value-format="YYYY-MM-DD" placeholder="按供应日期" clearable size="small" style="width:160px" />
          <el-select v-model="query.kitchenId" placeholder="按操作间" clearable size="small" style="width:160px">
            <el-option v-for="k in kitchens" :key="k.id" :label="k.name" :value="k.id" />
          </el-select>
          <el-button size="small" type="primary" @click="loadBatches">查询</el-button>
          <el-button size="small" type="success" @click="openBatch">排一批</el-button>
        </div>
      </template>
      <el-table :data="batches" border stripe size="small" v-loading="loading">
        <el-table-column prop="batchNo" label="批次号" width="110" />
        <el-table-column label="菜品" width="130">
          <template #default="{ row }">{{ dishName(row.dishId) }}</template>
        </el-table-column>
        <el-table-column label="操作间" width="130">
          <template #default="{ row }">{{ kitchenName(row.kitchenId) }}</template>
        </el-table-column>
        <el-table-column prop="serveDate" label="供应日" width="115" />
        <el-table-column prop="meal" label="餐次" width="80" />
        <el-table-column prop="planPortions" label="计划份" width="90" />
        <el-table-column prop="actualPortions" label="实际份" width="90" />
        <el-table-column prop="sampleWeight" label="留样(克)" width="100" />
        <el-table-column prop="chef" label="厨师" width="90" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === '已完成' ? 'success' : row.status === '已作废' ? 'info' : 'warning'">
              {{ row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="190">
          <template #default="{ row }">
            <el-button v-if="row.status === '备料中'" link type="primary" @click="advance(row, 'cook')">开火</el-button>
            <el-button v-if="row.status === '加工中'" link type="success" @click="openDone(row)">完工</el-button>
            <el-button
              v-if="row.status !== '已完成' && row.status !== '已作废'"
              link
              type="danger"
              @click="scrap(row)"
            >作废</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dishVisible" title="新增菜品" width="440px">
      <el-form label-width="90px">
        <el-form-item label="菜品编号"><el-input v-model="dishForm.code" placeholder="如 D-1006" /></el-form-item>
        <el-form-item label="菜名"><el-input v-model="dishForm.name" /></el-form-item>
        <el-form-item label="类别">
          <el-select v-model="dishForm.category" style="width:100%">
            <el-option v-for="c in ['荤菜', '素菜', '汤', '主食']" :key="c" :label="c" :value="c" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dishVisible = false">取消</el-button>
        <el-button type="primary" @click="submitDish">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="batchVisible" title="排一批" width="500px">
      <el-form label-width="110px">
        <el-form-item label="菜品">
          <el-select v-model="batchForm.dishId" placeholder="只列在售菜品" style="width:100%">
            <el-option v-for="d in sellingDishes" :key="d.id" :label="`${d.name}（${d.code}）`" :value="d.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="操作间">
          <el-select v-model="batchForm.kitchenId" placeholder="只列在用的操作间" style="width:100%">
            <el-option v-for="k in usableKitchens" :key="k.id" :label="k.name" :value="k.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="供应日期">
          <el-date-picker v-model="batchForm.serveDate" type="date" value-format="YYYY-MM-DD" style="width:100%" />
        </el-form-item>
        <el-form-item label="餐次">
          <el-radio-group v-model="batchForm.meal">
            <el-radio v-for="m in ['早餐', '午餐', '晚餐']" :key="m" :label="m">{{ m }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="计划份数">
          <el-input-number v-model="batchForm.planPortions" :min="1" :step="50" />
        </el-form-item>
        <el-form-item label="留样克数">
          <el-input-number v-model="batchForm.sampleWeight" :min="0" :step="25" />
        </el-form-item>
        <el-form-item label="厨师"><el-input v-model="batchForm.chef" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="batchVisible = false">取消</el-button>
        <el-button type="primary" @click="submitBatch">排下去</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="doneVisible" title="完工登记" width="420px">
      <el-form label-width="120px">
        <el-form-item label="实际做出来份数">
          <el-input-number v-model="donePortions" :min="1" :step="10" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="doneVisible = false">取消</el-button>
        <el-button type="primary" @click="submitDone">完工</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { batchApi, dishApi, kitchenApi } from '../api'

const statuses = ['备料中', '加工中', '已完成', '已作废']

const dishes = ref([])
const batches = ref([])
const kitchens = ref([])
const loading = ref(false)
const query = reactive({ status: '', date: '', kitchenId: null })

const dishVisible = ref(false)
const dishForm = reactive({ code: '', name: '', category: '荤菜' })
const batchVisible = ref(false)
const batchForm = reactive({
  dishId: null, kitchenId: null, serveDate: '', meal: '午餐',
  planPortions: 300, sampleWeight: 150, chef: ''
})
const doneVisible = ref(false)
const donePortions = ref(300)
const doneId = ref(null)

const sellingDishes = computed(() => dishes.value.filter((d) => d.status === '在售'))
const usableKitchens = computed(() => kitchens.value.filter((k) => k.status === '在用'))

const dishName = (id) => (id ? dishes.value.find((d) => d.id === id)?.name || `#${id}` : '—')
const kitchenName = (id) => (id ? kitchens.value.find((k) => k.id === id)?.name || `#${id}` : '—')

const loadDishes = async () => {
  dishes.value = await dishApi.list({})
}

const loadBatches = async () => {
  loading.value = true
  try {
    batches.value = await batchApi.list({
      status: query.status || undefined,
      date: query.date || undefined,
      kitchenId: query.kitchenId || undefined
    })
  } catch (e) {
    ElMessage.error(e.message)
  } finally {
    loading.value = false
  }
}

const openDish = () => {
  Object.assign(dishForm, { code: '', name: '', category: '荤菜' })
  dishVisible.value = true
}

const submitDish = async () => {
  try {
    await dishApi.create({ ...dishForm })
    ElMessage.success('菜品已新增')
    dishVisible.value = false
    await loadDishes()
  } catch (e) {
    ElMessage.error(e.message)
  }
}

const toggleDish = async (row) => {
  try {
    await dishApi.update(row.id, { status: row.status === '在售' ? '停用' : '在售' })
    ElMessage.success('已更新')
    await loadDishes()
  } catch (e) {
    ElMessage.error(e.message)
  }
}

const openBatch = () => {
  Object.assign(batchForm, {
    dishId: null, kitchenId: null,
    serveDate: new Date().toISOString().slice(0, 10),
    meal: '午餐', planPortions: 300, sampleWeight: 150, chef: ''
  })
  batchVisible.value = true
}

const submitBatch = async () => {
  try {
    await batchApi.open({ ...batchForm })
    ElMessage.success('这一批排下去了')
    batchVisible.value = false
    await loadBatches()
  } catch (e) {
    ElMessage.error(e.message)
  }
}

const advance = async (row, action) => {
  try {
    await batchApi.advance(row.id, action, null)
    ElMessage.success('已更新')
    await loadBatches()
  } catch (e) {
    ElMessage.error(e.message)
  }
}

const openDone = (row) => {
  doneId.value = row.id
  donePortions.value = row.planPortions
  doneVisible.value = true
}

const submitDone = async () => {
  try {
    await batchApi.advance(doneId.value, 'done', donePortions.value)
    ElMessage.success('已完工')
    doneVisible.value = false
    await loadBatches()
  } catch (e) {
    ElMessage.error(e.message)
  }
}

const scrap = async (row) => {
  try {
    await ElMessageBox.confirm(`确定把批次 ${row.batchNo} 作废？`, '提示')
  } catch {
    return
  }
  advance(row, 'scrap')
}

onMounted(async () => {
  try {
    const [d, k] = await Promise.all([dishApi.list({}), kitchenApi.list({})])
    dishes.value = d
    kitchens.value = k
  } catch (e) {
    ElMessage.error(e.message)
  }
  await loadBatches()
})
</script>
